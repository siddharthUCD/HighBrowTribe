package service.messages;

import lombok.Getter;
import lombok.Setter;
import service.centralCore.Tribe;

import java.util.HashSet;
import java.util.Set;

public class TribeSuggestionRequest implements MySerializable{
    @Getter
    @Setter
    private long uniqueId;
    @Getter
    @Setter
    private Set<Tribe> suggestedTribes;
    public TribeSuggestionRequest(){
    }
}
