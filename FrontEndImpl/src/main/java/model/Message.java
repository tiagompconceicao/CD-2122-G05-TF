package model;

public class Message {

    private MessageType type;
    private String value;

    public Message(MessageType type, String value){
        this.type = type;
        this.value = value;
    }

    public MessageType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
