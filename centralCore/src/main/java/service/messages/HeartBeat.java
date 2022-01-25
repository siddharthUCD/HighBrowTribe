package service.messages;

public class HeartBeat implements MySerializable{
    private String module;

    public HeartBeat(){}
    public HeartBeat(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }
}
