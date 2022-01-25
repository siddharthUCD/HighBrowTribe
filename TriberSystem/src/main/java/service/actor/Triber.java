package service.actor;

import akka.actor.*;
import scala.concurrent.duration.Duration;
import service.centralCore.*;
import service.messages.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Triber extends AbstractActor {

    //region Declaration
    private static ActorSystem system;
    private static long userUniqueId = 1;
    private static long tribeUniqueId = 1;
    private static ActorSelection interestsActor, persistanceActor, communicationActor;
    private static boolean isPersistanceModuleUp = true;
    private static boolean isCommunicatorModuleUp = true;
    private static boolean isRestModuleUp = true;
    private static boolean wasPersistanceModuleDown = false;
    private static boolean wasCommunicatorModuleDown = false;
    private static boolean wasRestModuleDown = false;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32;9m";

    HashMap<Long, UserInfo> requestsToUserInfoMap = new HashMap<>();
    HashMap<Long, Long> uniqueIdMap = new HashMap<>();
    ArrayList<Tribe> allTribes = new ArrayList<>();
    ArrayList<UserInfo> allUserInfo = new ArrayList<>();
    //endregion

    public static void main(String[] args){
        system = ActorSystem.create();
        ActorRef ref = system.actorOf(Props.create(Triber.class), "triber");

        //Appropriate module actor references
        interestsActor = system.actorSelection("akka.tcp://default@127.0.0.1:2554/user/interests");
        persistanceActor = system.actorSelection("akka.tcp://default@127.0.0.1:2552/user/userSystem");
        communicationActor = system.actorSelection("akka.tcp://default@127.0.0.1:2556/user/communicator");

        //Initializer call sent to Persistance actor to retreive all tribe/user details
        persistanceActor.tell("InitializeTriberSystem", ref);
        ref.tell("Send Pulse", null);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            //HeartBeat replies from the modules
            .match(HeartBeat.class,
                msg -> {
                    //Inform User when a module is up again
                    switch (msg.getModule()){
                        case "Rest Module":
                            if(wasRestModuleDown){
                                wasRestModuleDown = false;
                                System.out.println(ANSI_GREEN + "YaYY!! The Interests Module is back up!" + ANSI_RESET);
                            }
                            isRestModuleUp = true;
                            break;
                        case "Persistence Module":
                            if(wasPersistanceModuleDown){
                                wasPersistanceModuleDown = false;
                                System.out.println(ANSI_GREEN + "YaYY!! The Persistance Module is back up!" + ANSI_RESET);
                            }
                            isPersistanceModuleUp = true;
                            break;
                        case "Communicator Module":
                            if(wasCommunicatorModuleDown){
                                wasCommunicatorModuleDown = false;
                                System.out.println(ANSI_GREEN + "YaYY!! The Communicator Module is back up!" + ANSI_RESET);
                            }
                            isCommunicatorModuleUp = true;
                    }
                })
                .match(String.class,
                    msg->{
                        //Send heartbeat messages to all the module and inform user if any of them goes down
                        if(msg.equals("Send Pulse")) {
                            if (isRestModuleUp) {
                                isRestModuleUp = false;
                                interestsActor.tell(new HeartBeat(), getSelf());
                            } else {
                                System.out.println(ANSI_RED + "CRITICAL ERROR: Interests Module Down!" + ANSI_RESET);

                                wasRestModuleDown = true;
                                interestsActor.tell(new HeartBeat(), getSelf());
                            }
                            if (isPersistanceModuleUp) {
                                isPersistanceModuleUp = false;
                                persistanceActor.tell(new HeartBeat(), getSelf());
                            } else {
                                System.out.println(ANSI_RED + "CRITICAL ERROR: Persistance Module Down!" + ANSI_RESET);
                                persistanceActor.tell("InitializeTriberSystem", getSelf());
                                wasPersistanceModuleDown = true;
                                persistanceActor.tell(new HeartBeat(), getSelf());
                            }
                            if (isCommunicatorModuleUp) {
                                isCommunicatorModuleUp = false;
                                communicationActor.tell(new HeartBeat(), getSelf());
                            } else {
                                System.out.println(ANSI_RED + "CRITICAL ERROR: Communicator Module Down!" + ANSI_RESET);
                                wasCommunicatorModuleDown = true;
                                communicationActor.tell(new HeartBeat(), getSelf());
                            }
                        }
                        //Repeat the heartbeat messages every 10 secs
                        getContext().system().scheduler().scheduleOnce(
                            Duration.create(5, TimeUnit.SECONDS),
                            getSelf(),
                            "Send Pulse",
                            getContext().dispatcher(), null);
                    })
            //Hold tribe & user data from DB
            .match(TriberInitializationResponse.class,
                msg -> {
                    allUserInfo = msg.getAllUsers();
                    allTribes = msg.getAllTribes();
                    userUniqueId = msg.getMaxUserId();
                    tribeUniqueId = msg.getMaxTribeId();
                })
            //Handle user's request to register themselves
            .match(UserRequest.class,
                msg -> {
                    System.out.println("User creation request received for : " + msg.getNewUser().getName() + " with Unique ID: " + msg.getUniqueId());
                    int validationStatus = validateInputRequest(msg);
                    // 0 -> new user
                    // 1 -> incorrect details
                    // 2 -> relogging

                    if(validationStatus == 0) {
                        //Adding new student
                        long currUserUniqueId = ++userUniqueId;
                        uniqueIdMap.put(currUserUniqueId, msg.getUniqueId());
                        requestsToUserInfoMap.put(currUserUniqueId, msg.getNewUser());

                        //Request sent to Interests System to get user's GitHub details
                        interestsActor.tell(new InterestsRequest(currUserUniqueId, msg.getNewUser().getGitHubId()), getSelf());
                    }
                    else if(validationStatus == 1){
                        //Invalid student details
                        UserResponse userResponse = new UserResponse(msg.getUniqueId(),"Invalid user data, please try a different user detail");
                        ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:"+msg.getNewUser().getPortNumber()+"/user/"+msg.getUniqueId());

                        //Inform client regarding invalid details
                        clientActor.tell(userResponse, null);
                    }
                    else
                    {
                        UserInfo newUserInfo = getUserByGitHubId(msg.getNewUser().getGitHubId());

                        UpdateUserDetailRequest UUDR = new UpdateUserDetailRequest();
                        newUserInfo.setPortNumber(msg.getNewUser().getPortNumber());

                        UUDR.setNewUserInfo(newUserInfo);
                        UUDR.setOldUniqueId(msg.getUniqueId());
                        UUDR.setNewUniqueId(++userUniqueId);

                        //Requests sent to update user data in case user has connected from a new device
                        persistanceActor.tell(UUDR, getSelf());
                    }
            })
            //Db has been updated with the user's latest details
            .match(UpdateUserDetailResponse.class,
                msg -> {
                    persistanceActor.tell("InitializeTriberSystem", getSelf());

                    //Temporary actor reference of the student
                    ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:"+msg.getNewUserInfo().getPortNumber()+"/user/"+msg.getOldUniqueId());

                    UserCreationResponse userCreationResponse = new UserCreationResponse();
                    userCreationResponse.setUniqueId(msg.getNewUniqueId());
                    userCreationResponse.setTribeId(msg.getNewUserInfo().getTribeId());

                    //Student is informed about their successful registration/login into the system
                    clientActor.tell(userCreationResponse, null);
            })
            //Handiling interests response of user
            .match(InterestsResponse.class,
                msg -> {
                    //Invalid github Id
                    if(msg.getInterest().getProgrammingLanguages() == null){
                        UserResponse userResponse = new UserResponse(msg.getUniqueId(),"GitHub ID is invalid, Please use a different GitHub Id");
                        ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:"+requestsToUserInfoMap.get(msg.getUniqueId()).getPortNumber()+"/user/"+uniqueIdMap.get(msg.getUniqueId()));
                        clientActor.tell(userResponse, null);
                    }
                    //No Programming languages found for the user
                    else if (msg.getInterest().getProgrammingLanguages().size() == 0) {
                        UserResponse userResponse = new UserResponse(msg.getUniqueId(),"GitHub ID has no programming repositories, Please use a different GitHub Id");
                        ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:"+requestsToUserInfoMap.get(msg.getUniqueId()).getPortNumber()+"/user/"+uniqueIdMap.get(msg.getUniqueId()));
                        clientActor.tell(userResponse, null);
                    }
                    //Appropriate details found for the user
                    else {
                        System.out.println("Received Interests response from Interests System for User :" + requestsToUserInfoMap.get(msg.getUniqueId()).getName() + " Unique ID:" + msg.getUniqueId());
                        requestsToUserInfoMap.get(msg.getUniqueId()).setInterests(msg.getInterest());

                        TribeSuggestionRequest tribeSuggestionRequest = new TribeSuggestionRequest();

                        //Fetch tribe suggestion for the user based on his interest
                        Set<Tribe> suggestedTribe = getTribeSuggestions(msg.getInterest(), requestsToUserInfoMap.get(msg.getUniqueId()));

                        //When there are no suggested tribes for the user, a new tribe is created
                        if (suggestedTribe.size() == 0) {
                            System.out.println("New Tribe Creation as no tribe for the user interests exists");
                            String tribeLanguage = "";
                            tribeLanguage = msg.getInterest().getProgrammingLanguages().stream().findFirst().get();
                            long tribeID = ++tribeUniqueId;
                            requestsToUserInfoMap.get(msg.getUniqueId()).setTribeId(tribeID);
                            UserCreationRequest userCreationRequest = new UserCreationRequest(msg.getUniqueId(), requestsToUserInfoMap.get(msg.getUniqueId()), tribeLanguage);

                            //Tribe details for the user is sent to the persistance actor
                            persistanceActor.tell(userCreationRequest, getSelf());
                        }
                        //When there is only one suggested tribe the user will automatically ebcome part of it
                        else if (suggestedTribe.size() == 1) {
                            System.out.println("One member tribe exists for the user interests");
                            Tribe tribe = suggestedTribe.stream().findFirst().get();
                            UserInfo currUser = requestsToUserInfoMap.get(msg.getUniqueId());
                            String tribeLanguage = getTribeName(tribe);
                            currUser.setTribeId(tribe.getTribeId());
                            requestsToUserInfoMap.get(msg.getUniqueId()).setTribeId(tribe.getTribeId());
                            UserCreationRequest UCR = new UserCreationRequest(msg.getUniqueId(), currUser, tribeLanguage);

                            //Respond to user with their joined tribe details
                            persistanceActor.tell(UCR, getSelf());
                        }
                        //If where are multiple tribe suggestions the user can pick their favorite
                        else {
                            System.out.println("User must select the tribe he wishes to join");
                            UserInfo userInfo = requestsToUserInfoMap.get(msg.getUniqueId());
                            ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:" + userInfo.getPortNumber() + "/user/" + uniqueIdMap.get(msg.getUniqueId()));
                            tribeSuggestionRequest.setUniqueId(msg.getUniqueId());
                            tribeSuggestionRequest.setSuggestedTribes(suggestedTribe);

                            //Prompting the client to make tribe selection
                            clientActor.tell(tribeSuggestionRequest, null);
                        }
                    }
                })
            //Provides communicator with the student's tribe
            .match(TribeDetailRequest.class,
                msg -> {
                    Tribe tribe = getTribeById(msg.getTribeId());
                    getSender().tell(new TribeDetailResponse(msg.getUniqueId(), tribe), null);
                })
            //Student has responded with their choice of tribe
            .match(TribeSuggestionResponse.class,
                msg->{
                    requestsToUserInfoMap.get(msg.getUniqueId()).setTribeId(msg.getTribeId());
                    UserCreationRequest userCreationRequest = new UserCreationRequest(msg.getUniqueId(),requestsToUserInfoMap.get(msg.getUniqueId()),getTribeName(getTribeById(msg.getTribeId())));

                    //Request sent to persistance actor to store student's tribe choice
                    persistanceActor.tell(userCreationRequest,getSelf());
                })

            //Persistance actor has successfully stored the student's details
            .match(UserCreationResponse.class,
                msg->{
                    System.out.println("You are successfully registered in the System");
                    long clientID = uniqueIdMap.get(msg.getUniqueId());
                    UserInfo userInfo = requestsToUserInfoMap.get(msg.getUniqueId());
                    ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:"+userInfo.getPortNumber()+"/user/"+clientID);
                    persistanceActor.tell("InitializeTriberSystem", getSelf());

                    //Student is informed with their registerd data
                    clientActor.tell(msg,null);
                })
            .build();
    }

    //Authenticate user and determine their category among the 3
    private int validateInputRequest(UserRequest userRequest){
        //User is logging in again
        if(allUserInfo!= null && allUserInfo.size()>0 && allUserInfo.stream().filter(user->user.getGitHubId()
                        .equalsIgnoreCase(userRequest.getNewUser().getGitHubId()) && user.getName().equalsIgnoreCase(userRequest.getNewUser().getName()))
                .collect(Collectors.toList()).size() > 0){

            return 2;
        }
        //User has incorrect details
        else if(allUserInfo!= null && allUserInfo.size()>0 && allUserInfo.stream().filter(user->user.getGitHubId()
                        .equalsIgnoreCase(userRequest.getNewUser().getGitHubId()) && !user.getName().equalsIgnoreCase(userRequest.getNewUser().getName()))
                .collect(Collectors.toList()).size() > 0){
            return 1;
        }
        //New user
        else{
            return 0;
        }
    }

    //Get tribe name based on Tribe ID
    private String getTribeName(Tribe tribe) {
        return allTribes.stream()
                .filter(t->t.getTribeId() == tribe.getTribeId())
                .collect(Collectors.toList()).get(0).getTribeName();
    }

    //Generate the potential tribes based on user's interest
    private Set<Tribe> getTribeSuggestions(Interests interests, UserInfo userInfo){
        Set<Tribe> filteredTribes = new HashSet<>();
        Set<String> remainingLanguages = interests.getProgrammingLanguages();

        //Matches the student's interests with the available tribes
        for(Tribe existingTribe:allTribes){
            Set<String> temp
                    = Stream.of(existingTribe.getTribeLanguages().trim().split("\\s*,\\s*"))
                    .collect(Collectors.toSet());
            if(containsMatch(interests.getProgrammingLanguages(),temp)){
                filteredTribes.add(existingTribe);
            }
            remainingLanguages.remove(existingTribe.getTribeName());
        }

        //Create new potentials tribes based on students interests
        if(remainingLanguages.stream().count() > 0){
            for(String language:remainingLanguages){
                Tribe tempTribe = new Tribe(++tribeUniqueId, language, interests.getProgrammingLanguages().stream().collect(Collectors.joining(",")), Arrays.asList(userInfo));
                filteredTribes.add(tempTribe);
                allTribes.add(tempTribe);
            }
        }

        return filteredTribes;
    }

    //Get the tribe object based on ID
    private Tribe getTribeById(long tribeId){
        return allTribes.stream().filter(x->x.getTribeId() == tribeId).collect(Collectors.toList()).get(0);
    }

    //Identify a student based on their github id
    private UserInfo getUserByGitHubId(String userGithubId) {
        return allUserInfo.stream()
                .filter(t->t.getGitHubId().equalsIgnoreCase(userGithubId))
                .collect(Collectors.toList()).get(0);
    }

    //Determines matches between tribe programming languages and student's programming languages
    private boolean containsMatch(Set<String> a, Set<String> b){

        Set<String> intersectSet = a.stream()
                .filter(b::contains)
                .collect(Collectors.toSet());
        if(intersectSet.size()>0){
            return true;
        }
        else {
            return false;
        }

    }
}
