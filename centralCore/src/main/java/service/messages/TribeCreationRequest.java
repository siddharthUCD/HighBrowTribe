package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.Tribe;
import service.centralCore.UserInfo;

public class TribeCreationRequest implements MySerializable{
    @Getter
    @Setter
    private UserInfo userInfo;
    @Getter
    @Setter
    private Tribe tribe;

    public TribeCreationRequest(UserInfo userInfo, Tribe tribe){
        this.userInfo = userInfo;
        this.tribe = tribe;
    }
}
