package service.centralCore;

import lombok.Getter;
import lombok.Setter;
import service.messages.MySerializable;

import java.util.List;

public class Tribe implements MySerializable {
    @Getter
    @Setter
    private long tribeId;
    @Getter
    @Setter
    private String tribeName;
    @Getter
    @Setter
    private String tribeLanguages;
    @Getter
    @Setter
    private List<UserInfo> members;



    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(UserInfo member:members){
            sb.append(member.getName() + ", ");
        }

        return "Tribe Id: " + tribeId + ", Tribe Name: " + tribeName + "_Tribe, Language: " + tribeLanguages + ", Members: " + sb.toString().substring(0,sb.length()-2);
    }

    public  Tribe(){};

    public Tribe(long tribeId, String tribeName, String tribeLanguages, List<UserInfo> members){
        this.tribeId = tribeId;
        this.tribeName = tribeName;
        this.tribeLanguages = tribeLanguages;
        this.members = members;
    }
}
