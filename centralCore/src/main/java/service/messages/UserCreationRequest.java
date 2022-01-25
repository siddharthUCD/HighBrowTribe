package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.UserInfo;

public class UserCreationRequest implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private UserInfo newUser;
    @Getter
    @Setter
    private String tribeLanguage;

    public UserCreationRequest(long uniqueId, UserInfo newUser, String tribeLanguage) {
        this.uniqueId = uniqueId;
        this.newUser = newUser;
        this.tribeLanguage = tribeLanguage;
    }

}
