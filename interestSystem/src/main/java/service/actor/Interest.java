package service.actor;

import akka.actor.*;
import jnr.ffi.annotations.In;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import service.centralCore.*;
import service.messages.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Interest system actor to receive and send message
 * Also has the main class to run the actor
 */
public class Interest extends AbstractActor {
    static ActorSystem system ;
    ActorSelection matcherActor = system.actorSelection("akka.tcp://default@127.0.0.1:2557/user/triber");

    //GitHub url
    private String githubUrl = "https://api.github.com/users/";
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HeartBeat.class,
                        msg -> {
                            getSender().tell(new HeartBeat("Rest Module"), null);
                        })
                .match(InterestsRequest.class,
                        msg -> {
                            HashSet<String> programmingLanguages = getUserLanguages(msg.getGithubUserId());
                            Interests interests = new Interests(programmingLanguages);
                            InterestsResponse interestsResponse = new InterestsResponse(msg.getRequestId()
                                    ,interests);
                            //sends interest's back to triber system
                            matcherActor.tell(interestsResponse,getSelf());

                        }).build();
    }

    /**
     * returns all programming languages of an users github id
     *
     * @param userId - git hub id
     * @return - all user languages from GitHub
     */
    private HashSet<String> getUserLanguages(String userId){
        JSONArray outputFromGitHub = callExternalSystem(githubUrl+userId+"/repos");
        return outputFromGitHub!= null ? userProgrammingLanguageInterests(outputFromGitHub):null;
    }

    /**
     * fetches all programming languages in GitHub langaugases from project repositories, stores in jasonArray and sends backs
     *
     * @param url - github url
     * @return - array of jason objects
     */
    private JSONArray callExternalSystem(String url) {
        RestTemplate restTemplate = new RestTemplate();
        try{
            ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);
            JSONArray jsonArray = new JSONArray(response.getBody());
            return jsonArray;
        }
        catch (HttpClientErrorException ex){
            System.out.println("Invalid User Id, Not found in GitHub");
            return null;
        }

    }

    /**
     * changes jason array of programming languages into string objects
     *
     * @param jsonArray - jasonArray of objects
     * @return - set of all programming languages
     */
    private HashSet<String> userProgrammingLanguageInterests(JSONArray jsonArray){
        Set<String> programmingLanguageInterests = IntStream.range(0,jsonArray.length())
                .mapToObj(index -> ((JSONObject)jsonArray.get(index)).optString("language"))
                .filter(element-> (element!=null && element != ""))
                .collect(Collectors.toSet());

        return new HashSet<>(programmingLanguageInterests);
    }
    public static void main(String[] args){
        system = ActorSystem.create();
        ActorRef ref = system.actorOf(Props.create(Interest.class), "interests");
    }
}
