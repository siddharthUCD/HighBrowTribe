package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.Tribe;
import service.centralCore.UserInfo;

import java.io.Serializable;
import java.util.ArrayList;

public class TriberInitializationResponse implements MySerializable {
    @Getter
    @Setter
    private ArrayList<UserInfo> allUsers;
    @Getter
    @Setter
    private ArrayList<Tribe> allTribes;
    @Getter
    @Setter
    private Long maxUserId;
    @Getter
    @Setter
    private Long maxTribeId;

    public TriberInitializationResponse(ArrayList<UserInfo> allUsers, ArrayList<Tribe> allTribes, Long maxUserId, Long maxTribeId){
        this.allUsers = allUsers;
        this.allTribes = allTribes;
        this.maxUserId = maxUserId;
        this.maxTribeId = maxTribeId;
    }
}
