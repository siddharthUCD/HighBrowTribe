package service.messages;

import lombok.Getter;
import lombok.Setter;

public class TribeDetailRequest implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private long tribeId;

    public TribeDetailRequest(){}
    public TribeDetailRequest(long uniqueId,long tribeId) {
        this.uniqueId = uniqueId;
        this.tribeId = tribeId;
    }
}