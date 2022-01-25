package service.messages;

import lombok.Getter;
import lombok.Setter;

public class ProblemSolvedResponse implements MySerializable {
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private long tribeId;
    @Getter
    @Setter
    private String errorMessage;

    public ProblemSolvedResponse(){};

    public ProblemSolvedResponse(long uniqueId, long tribeId) {
        this.uniqueId = uniqueId;
        this.tribeId = tribeId;
    }
}
