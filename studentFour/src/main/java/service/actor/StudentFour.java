package service.actor;

import akka.actor.*;
import service.centralCore.Tribe;
import service.centralCore.UserInfo;
import service.messages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Random;

public class StudentFour extends AbstractActor {
    //region Declarations
    private static ActorSystem system;
    private static ActorRef ref;
    private static ActorSelection communicationSelection;
    private static ActorSelection triberSelection;
    private static UserInfo userInfo;
    private static UserRequest userRequest;
    private static Boolean IsInChat = false;
    private static int portNumber = 2560;
    //endregion

    public static void main(String [] args) {
        //region Declaration
        system = ActorSystem.create();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        //Selection for the communicator Actor
        communicationSelection =
                system.actorSelection("akka.tcp://default@127.0.0.1:2556/user/communicator");
        //Selection for the Triber Actor
        triberSelection =
                system.actorSelection("akka.tcp://default@127.0.0.1:2557/user/triber");
        userInfo = new UserInfo();
        Random rand = new Random();
        //endregion

        //Generates a unique id for the student
        long uniqueId = Math.abs(rand.nextInt() + rand.nextInt());

        userInfo.setPortNumber(portNumber);
        System.out.println("Client is starting up...");
        try {
            //Delays print statements until actor initialization prints are over
            Thread.sleep(3000);
            //Read user details post appropriate validations
            while(true) {
                System.out.println("Enter your name");
                String userName = reader.readLine();
                System.out.println("Enter your GitHub Id");
                String githubId = reader.readLine();

                if(userName.trim().length() > 0 && githubId.trim().length() > 0){
                    userInfo.setGitHubId(githubId.trim());
                    userInfo.setName(userName.trim());
                    setReference(uniqueId);
                    break;
                }
                else{
                    System.out.println("Name and Github ID cannot be empty");
                }
            }
        }
        catch (IOException ex){
            System.out.println("An error occured, please restart.");
        }
        catch (InterruptedException ex){
            System.out.println("An error occured, please restart.");
        }
        userRequest = new UserRequest(uniqueId,userInfo);

        System.out.println("User has sent newUserRequest to Triber");

        //Actor sends a request to triber to register themselves on to the system
        triberSelection.tell(userRequest, ref);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return  receiveBuilder()
                //Student has successfully registered to their tribe's chat
                .match(ChatRegisterResponse.class, msg->{
                    Tribe currentTribe = msg.getTribe();
                    System.out.println("You have joined the community dialogue of " + currentTribe.getTribeName());

                    //Enables chat on a separate thread
                    Thread thread = new Thread(() -> {
                        IsInChat = true;
                        ParticipateInChat();
                    });
                    thread.start();
                })
                //Receive a chat message from other tribe members
                .match(ChatMessageReceive.class, msg->{
                    System.out.println("[" + msg.getSentTime() + "] " + msg.getSenderName() + ": " + msg.getMessage());

                    Thread thread = new Thread(this::ParticipateInChat);

                    //Enables chat if disabled
                    if(!IsInChat){
                        IsInChat = true;
                        thread.start();
                    }
                })
                //Student has been successfully registered into the system
                .match(UserCreationResponse.class,msg->{
                    long uniqueId = msg.getUniqueId();

                    System.out.println("You are redirected to register for the Tribe's group chat...");

                    //Update's registration details to the user
                    userRequest.setUniqueId(uniqueId);
                    userInfo.setTribeId(msg.getTribeId());

                    //Updates the actor reference when the unique ID has changed
                    String tempRef = ref.toString();
                    int startIndex = tempRef.lastIndexOf("/")+1;
                    int endIndex = tempRef.indexOf("#");
                    String uid = tempRef.substring(startIndex,endIndex);
                    long uniqueIdtemp = Long.parseLong(uid);
                    if(uniqueIdtemp != uniqueId){
                        setReference(uniqueId);
                    }

                    //Student requests communicator system to enable chat with their tribe
                    ChatRegisterRequest chatRegisterRequest = new ChatRegisterRequest(uniqueId,userInfo);
                    communicationSelection.tell(chatRegisterRequest,getSelf());
                })
                //Student is asked to re-enter their details
                .match(UserResponse.class,msg->{
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(System.in));
                    System.out.println("Incorrect student details, please re-enter");

                    //Read user details post appropriate validations
                    while(true) {
                        System.out.println("Enter your name");
                        String userName = reader.readLine();
                        System.out.println("Enter your GitHub Id");
                        String githubId = reader.readLine();

                        if(userName.trim().length() > 0 && githubId.trim().length() > 0){
                            userInfo.setName(userName.trim());
                            userInfo.setGitHubId(githubId.trim());
                            break;
                        }
                        else{
                            System.out.println("Name and Github ID cannot be empty");
                        }
                    }

                    //Requests triber system for registration/login
                    triberSelection.tell(userRequest, ref);
                })
                //Receives a list of potential tribes they can join
                .match(TribeSuggestionRequest.class, msg->{
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(System.in));
                    StringBuilder sb;
                    System.out.println("Received following tribe suggestions for you !");

                    //Displays the tribe suggestions to the student
                    for(Tribe tribe:msg.getSuggestedTribes()){
                        sb = new StringBuilder();

                        for(UserInfo UI:tribe.getMembers()){
                            sb.append(UI.getName()).append(", ");
                        }

                        System.out.println(tribe);
                    }

                    System.out.println("Enter the ID of the tribe you would like to join");
                    String tribeIdString = reader.readLine();

                    try {
                        long tribeId = Long.parseLong(tribeIdString);
                        userRequest.setUniqueId(msg.getUniqueId());

                        setReference(msg.getUniqueId());
                        userInfo.setTribeId(tribeId);

                        //Respond to triber with the student's choice of tribe
                        TribeSuggestionResponse tribeSuggestionResponse = new TribeSuggestionResponse(msg.getUniqueId(),tribeId);
                        triberSelection.tell(tribeSuggestionResponse, getSelf());
                    }
                    catch(Exception ex){
                        System.out.print("Invalid Input! Restart application");
                    }
                }).build();
    }

    //Listening to the student's keyboard when the chat dialogue is open
    private void ParticipateInChat() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            // Reading data using readLine
            String message;
            Timestamp ts;
            while(true) {
                message = reader.readLine();

                //Catches the time of chat sent
                ts = new Timestamp(System.currentTimeMillis());

                //Sends the chat message to the communicator
                communicationSelection.tell(new ChatMessageSend(userInfo.getName(), ts, userRequest.getUniqueId(), userInfo.getTribeId(), message), getSelf());
            }
        }
        catch(IOException ex){
            System.out.println("Error occured!");
        }
    }

    //Updates the student's actor reference
    private static void setReference(Long UniqueId){
        ref = system.actorOf(Props.create(StudentFour.class), UniqueId.toString());
    }
}
