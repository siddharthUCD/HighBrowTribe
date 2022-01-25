package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.UserInfo;

public class UserRequest implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private UserInfo newUser;

    public UserRequest(){};

    public UserRequest(long uniqueId, UserInfo newUser) {
        this.uniqueId = uniqueId;
        this.newUser = newUser;
    }
}
