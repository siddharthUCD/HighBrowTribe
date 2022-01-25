package service.messages;

import lombok.Getter;
import lombok.Setter;

public class UserCreationResponse implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private long tribeId;
    public UserCreationResponse(){}

}
