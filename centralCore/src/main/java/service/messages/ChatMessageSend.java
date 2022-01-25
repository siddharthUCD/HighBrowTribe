package service.messages;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

public class ChatMessageSend implements MySerializable {
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private String senderName;
    @Getter
    @Setter
    private Timestamp sentTime;
    @Getter
    @Setter
    private long tribeId;
    @Getter
    @Setter
    private String message;

    public ChatMessageSend(){};
    public ChatMessageSend(String senderName, Timestamp sentTime, long uniqueId,long tribeId, String message) {
        this.senderName = senderName;
        this.sentTime = sentTime;
        this.uniqueId = uniqueId;
        this.tribeId = tribeId;
        this.message = message;
        //Instant temp;
    }

//    public long getUniqueId() {
//        return uniqueId;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public String getSenderName() {
//        return senderName;
//    }
//
//    public Timestamp getSentTime() {
//        return sentTime;
//    }
}
