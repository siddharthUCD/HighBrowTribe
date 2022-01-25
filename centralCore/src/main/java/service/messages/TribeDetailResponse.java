package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.Tribe;

public class TribeDetailResponse implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private Tribe tribe;

    public TribeDetailResponse(){};
    public TribeDetailResponse(long uniqueId, Tribe tribe) {
        this.uniqueId = uniqueId;
        this.tribe = tribe;
    }
}