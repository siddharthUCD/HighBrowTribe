package service.centralCore;

import lombok.Getter;
import lombok.Setter;
import service.messages.MySerializable;

import java.util.HashSet;

public class UserInfo implements MySerializable {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String gitHubId;
    @Getter
    @Setter
    private Interests interests;
    @Getter
    @Setter
    private int PortNumber;
    @Getter
    @Setter
    private long tribeId;

    public UserInfo(){}
    public UserInfo(String name, String gitHubId){
        this.name = name;
        this.gitHubId = gitHubId;
        this.interests = null;
    }
}
