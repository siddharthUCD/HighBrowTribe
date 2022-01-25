package service.centralCore;

import lombok.Getter;
import lombok.Setter;
import service.messages.MySerializable;

import java.util.Set;

public class Interests implements MySerializable {
    @Getter
    @Setter
    private Set<String> programmingLanguages;
    public Interests(){}
    public Interests(Set<String> programmingLanguages){
        this.programmingLanguages = programmingLanguages;
    }

}
