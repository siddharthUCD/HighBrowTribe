package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.UserInfo;

public class UpdateUserDetailRequest implements MySerializable{
    @Getter
    @Setter
    private Long oldUniqueId;
    @Getter
    @Setter
    private Long newUniqueId;
//    @Getter
//    @Setter
//    private UserInfo oldUserInfo;
    @Getter
    @Setter
    private UserInfo newUserInfo;
}