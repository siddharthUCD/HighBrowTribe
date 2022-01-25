package com.example.Tribes.service.UserSystem;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.example.Tribes.service.actors.Persistent;

public class Main {
    public static void main(String[] args){
        //start persistence server
        ActorSystem system = ActorSystem.create();
        system.actorOf(Props.create(Persistent.class), "userSystem");
    }
}
