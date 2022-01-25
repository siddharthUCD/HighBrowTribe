package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.Interests;

public class InterestsResponse implements MySerializable {
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private Interests interest;
    public InterestsResponse(){};
    public InterestsResponse(long uniqueId, Interests interest) {
        this.uniqueId = uniqueId;
        this.interest = interest;
    }
}
