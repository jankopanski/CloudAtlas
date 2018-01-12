package pl.edu.mimuw.cloudatlas.modules;

import lombok.Data;

enum msgType {
    idk, MsgNotUnderstood, Timer, RMICall
}

@Data
public class Message {
    public Module source;
    public Module destination;
    public msgType type;
    MessageContent content;
}
