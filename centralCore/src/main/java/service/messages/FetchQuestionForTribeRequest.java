package service.messages;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

public class FetchQuestionForTribeRequest implements MySerializable {
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private long tribeId;

    public FetchQuestionForTribeRequest(){};

    public FetchQuestionForTribeRequest(long uniqueId, long tribeId) {
        this.uniqueId = uniqueId;
        this.tribeId = tribeId;
    }
}
