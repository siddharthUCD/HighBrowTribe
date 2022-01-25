package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.UserInfo;

public class ChatRegisterRequest implements MySerializable {

    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private UserInfo userInfo;

    public ChatRegisterRequest(){};
    public ChatRegisterRequest(long uniqueId,UserInfo userInfo) {
        this.uniqueId = uniqueId;
        this.userInfo = userInfo;

    };

}
