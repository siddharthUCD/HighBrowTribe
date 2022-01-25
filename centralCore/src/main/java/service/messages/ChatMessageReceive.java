package service.messages;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

public class ChatMessageReceive implements MySerializable {
    @Getter
    @Setter
    private String senderName;
    @Getter
    @Setter
    private Timestamp sentTime;
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private String message;

    public ChatMessageReceive(){};
    public ChatMessageReceive(String senderName, Timestamp sentTime, long uniqueId, String message) {
        this.senderName = senderName;
        this.sentTime = sentTime;
        this.uniqueId = uniqueId;
        this.message = message;
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
