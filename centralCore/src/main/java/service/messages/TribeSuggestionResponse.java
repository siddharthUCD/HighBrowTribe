package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.Tribe;

import java.util.HashSet;

public class TribeSuggestionResponse implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private long tribeId;
    public TribeSuggestionResponse(){
    }
    public TribeSuggestionResponse(long uniqueId,long tribeId){
        this.uniqueId = uniqueId;
        this.tribeId = tribeId;
    }
}
