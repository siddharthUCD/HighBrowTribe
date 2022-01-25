package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.UserInfo;

public class UserResponse implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private String errorMessage;

    public UserResponse(){};

    public UserResponse(long uniqueId, String errorMessage) {
        this.uniqueId = uniqueId;
        this.errorMessage = errorMessage;
    }
}
