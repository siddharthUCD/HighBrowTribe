package service.messages;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

public class FetchQuestionForTribeResponse implements MySerializable {
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private long tribeId;
    @Getter
    @Setter
    private String question;

    public FetchQuestionForTribeResponse(){};

    public FetchQuestionForTribeResponse(long uniqueId, long tribeId,String question) {
        this.uniqueId = uniqueId;
        this.tribeId = tribeId;
        this.question = question;
    }
}
