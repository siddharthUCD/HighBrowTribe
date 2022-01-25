package service.messages;

import lombok.Getter;
import lombok.Setter;

public class InterestsRequest implements MySerializable {
    @Getter
    @Setter
    private long requestId;
    @Getter
    @Setter
    private String githubUserId;
    public InterestsRequest(){}
    public InterestsRequest(long requestId, String githubUserId) {
        this.requestId = requestId;
        this.githubUserId = githubUserId;
    }
}
