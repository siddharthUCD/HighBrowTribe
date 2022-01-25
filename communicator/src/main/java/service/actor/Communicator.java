package service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import service.centralCore.UserInfo;
import service.messages.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Communicator extends AbstractActor {
    //region Declaration
    private static ActorSystem system;
    //Selection of the Triber system
    private ActorSelection TriberActor = system.actorSelection("akka.tcp://default@127.0.0.1:2557/user/triber");
    //Maps the user's github ID to their unique ID
    private static HashMap<String, Long> gitHubIdRequestId = new HashMap<>();
    //Maintains all the active users with the list of their tribe members
    private static HashMap<Long, List<UserInfo>> ActiveUsers = new HashMap<>();
    //Maintains the user ports
    private static HashMap<Long, Integer> UserPorts = new HashMap<>();
    //Selection for the database system
    private static ActorSelection persistanceActor;
    //endregion

    public static void main(String [] args){
        system = ActorSystem.create();
        system.actorOf(Props.create(Communicator.class), "communicator");
        persistanceActor = system.actorSelection("akka.tcp://default@127.0.0.1:2552/user/userSystem");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //Replies back to the triber's heartbeat message with an identification message
                .match(HeartBeat.class,
                        msg -> {
                            getSender().tell(new HeartBeat("Communicator Module"), null);
                        })
                //Indicates all the tribe members that the problem has been solved
                .match(ProblemSolvedResponse.class,
                        msg -> {
                            ActorSelection selection;
                            for (UserInfo user : ActiveUsers.get(msg.getTribeId())) {
                                long uniqueId = gitHubIdRequestId.get(user.getGitHubId());
                                System.out.println("PortNumber : " + user.getPortNumber() + " Unique Id : " + uniqueId);
                                selection = system.actorSelection("akka.tcp://default@127.0.0.1:" + user.getPortNumber() + "/user/" + uniqueId);

                                //Informs the members of the tribe that a question has been solved
                                selection.tell(new ChatMessageReceive("Bot : ", new Timestamp(System.currentTimeMillis()), msg.getUniqueId(), msg.getErrorMessage()), null);
                            }
                        })
                //Handles a client's request to join their respective tribe's chat forum
                .match(ChatRegisterRequest.class,
                        msg -> {
                            long tribeId = msg.getUserInfo().getTribeId();
                            long uniqueId = msg.getUniqueId();
                            System.out.println("Chat register request received from User : "+msg.getUserInfo().getName()+" Unique ID: " + msg.getUniqueId());
                            gitHubIdRequestId.put(msg.getUserInfo().getGitHubId(),uniqueId);
                            ActiveUsers.put(tribeId,null);
                            UserPorts.put(uniqueId, msg.getUserInfo().getPortNumber());

                            //Requests triber actor to provied tribe details of a student
                            TriberActor.tell(new TribeDetailRequest(uniqueId,tribeId), getSelf());
                        })
                //Handles the triber's reply with the student's tribe details
                .match(TribeDetailResponse.class,
                        msg -> {
                            ActiveUsers.put(msg.getTribe().getTribeId(),msg.getTribe().getMembers());
                            int PortNumber = UserPorts.get(msg.getUniqueId());
                            ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:" + PortNumber + "/user/" + msg.getUniqueId());

                            //Inform the student that they have been successfully added to the tribe chat
                            clientActor.tell(new ChatRegisterResponse(msg.getTribe()), null);
                        })
                //Receives client's message and checks if its a code else broadcasts the message to the other tribe members
                .match(ChatMessageSend.class,
                        msg -> {
                            ActorSelection selection;
                            if(msg.getMessage().equals("!problem question")){
                              //Make call to persistance actor to fetch question for the tribe
                              persistanceActor.tell(new FetchQuestionForTribeRequest(msg.getUniqueId(),msg.getTribeId()),getSelf());
                            }
                            else if(msg.getMessage().equals("!problem solved")){
                              //Informs the persistance actor that a problem has been solved by a tribe
                              persistanceActor.tell(new TribeDetailRequest(msg.getUniqueId(),msg.getTribeId())
                                        ,getSelf());
                            }
                            else {
                                for (UserInfo user : ActiveUsers.get(msg.getTribeId())) {
                                    long uniqueId = gitHubIdRequestId.get(user.getGitHubId());
                                    if (uniqueId != msg.getUniqueId()) {
                                        System.out.println("PortNumber : " + user.getPortNumber() + " Unique Id : " + uniqueId);
                                        selection = system.actorSelection("akka.tcp://default@127.0.0.1:" + user.getPortNumber() + "/user/" + uniqueId);

                                        //Relays a message from a fellow member to all the members of the tribe
                                        selection.tell(new ChatMessageReceive(msg.getSenderName(), msg.getSentTime(), msg.getUniqueId(), msg.getMessage()), null);
                                    }
                                }
                            }
                        })
                //Receives the question from the database
                .match(FetchQuestionForTribeResponse.class, msg->{
                    for (UserInfo user : ActiveUsers.get(msg.getTribeId())) {
                        long uniqueId = gitHubIdRequestId.get(user.getGitHubId());
                        if (uniqueId == msg.getUniqueId()) {
                            System.out.println("PortNumber : " + user.getPortNumber() + " Unique Id : " + uniqueId);
                            ActorSelection selection = system.actorSelection("akka.tcp://default@127.0.0.1:" + user.getPortNumber() + "/user/" + uniqueId);

                            //Informs the tribe members with the current question they have to solve
                            selection.tell(new ChatMessageReceive("Bot : ", new Timestamp(System.currentTimeMillis()), msg.getUniqueId(), msg.getQuestion()), null);
                        }
                    }
                }).build();
    }
}
