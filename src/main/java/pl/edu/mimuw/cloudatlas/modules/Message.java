package pl.edu.mimuw.cloudatlas.modules;

import lombok.Data;

enum msgType {
    idk, MsgNotUnderstood
}

@Data
public class Message {
    Module source;
    Module destination;
    msgType type;
    MessageContent content;
}
