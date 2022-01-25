package com.example.Tribes.service.actors;

import akka.actor.AbstractActor;
import com.example.Tribes.Repo.Constants;
import com.example.Tribes.TribesApplication;
import service.messages.*;

import java.util.Random;

/**
 * Persistent actor class for Persistent module
 */
public class Persistent extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(HeartBeat.class,
                msg -> {
                    // sends the heartbeat message back when service is up and running
                    getSender().tell(new HeartBeat("Persistence Module"), null);
                })
            .match(UserCreationRequest.class,
            msg -> {
                //starts DB if Database is not running
                if(Constants.configurableApplicationContext == null) {
                    TribesApplication.main(new String[0]);
                }
                //Persists data in User table coming from triber system
                TribesApplication.setUserInfo(msg.getUniqueId(),msg.getNewUser(),msg.getTribeLanguage());
                System.out.println("Send user creation response to the triber system..For Unique ID: "+msg.getUniqueId()+" Port: "
                +msg.getNewUser().getPortNumber()+" Tribe Language="+msg.getTribeLanguage());
                //sends unique id back to triber system after persistence
                UserCreationResponse userCreationResponse = new UserCreationResponse();
                userCreationResponse.setUniqueId(msg.getUniqueId());
                userCreationResponse.setTribeId(msg.getNewUser().getTribeId());
                getSender().tell(userCreationResponse,self());
            })
            .match(FetchQuestionForTribeRequest.class, msg->{
                String question = "";
                FetchQuestionForTribeResponse fetchQuestionForTribeResponse;
                //starts DB if Database is not running
                if(Constants.configurableApplicationContext == null) {
                    TribesApplication.main(new String[0]);
                }

                //sends the programming questions to triber system
                if(TribesApplication.getTribeQuestionDetails(msg.getTribeId()) != null){
                    question = TribesApplication.getTribeQuestionDetails(msg.getTribeId()).getQuestion();
                    System.out.println("Question in if: ="+question);
                    fetchQuestionForTribeResponse = new FetchQuestionForTribeResponse(msg.getUniqueId(),
                            msg.getTribeId(), question);
                }
                else{
                    int index = new Random().nextInt(24);
                    //checks for new question to be sent to triber system when current challange is completed
                    question = TribesApplication.getQuestion().get(index).getQuestion();
                    System.out.println("Question in else: ="+question);
                    fetchQuestionForTribeResponse = new FetchQuestionForTribeResponse(msg.getUniqueId(),
                            msg.getTribeId(), question);
                    TribesApplication.setTribeQuestionDetails(msg.getTribeId(), question);
                }
                getSender().tell(fetchQuestionForTribeResponse, self());
            })
            .match(TribeDetailRequest.class,msg->{
                //starts DB if Database is not running
                if(Constants.configurableApplicationContext == null) {
                    TribesApplication.main(new String[0]);
                }
                // updates DB with the current programming challenge and removes old ones
                ProblemSolvedResponse problemSolvedResponse = new ProblemSolvedResponse(msg.getUniqueId(),msg.getTribeId());
                if(TribesApplication.getTribeQuestionDetails(msg.getTribeId()) != null){
                    TribesApplication.deleteTribeQuestionDetails(msg.getTribeId());
                    problemSolvedResponse.setErrorMessage("Congratulations on completing the challenge, You may now request a new challenge by entering !problem question");
                }
                else{
                    problemSolvedResponse.setErrorMessage("No challenge associated with the tribe, Please request a challenge by entering !problem question");
                }
                getSender().tell(problemSolvedResponse, self());
            })
            .match(String.class,
            msg -> {
                //starts DB if Database is not running
                if(Constants.configurableApplicationContext == null) {
                    TribesApplication.main(new String[0]);
                }
                //sends all user info to triber when system starts
                if(msg.equals("InitializeTriberSystem")){
                    getSender().tell(TribesApplication.getAllUserInfo(), self());
                }
            })
            .match(UpdateUserDetailRequest.class,
            msg -> {
                UpdateUserDetailResponse UDR = new UpdateUserDetailResponse();
                //starts DB if Database is not running
                if(Constants.configurableApplicationContext == null) {
                    TribesApplication.main(new String[0]);
                }
                //updates user db with updated values
                UDR.setNewUserInfo(TribesApplication.updateUserInfo(msg.getNewUniqueId(), msg.getNewUserInfo()));
                UDR.setOldUniqueId(msg.getOldUniqueId());
                UDR.setNewUniqueId(msg.getNewUniqueId());

                getSender().tell(UDR, null);
            }).build();
    }
}