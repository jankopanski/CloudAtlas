package pl.edu.mimuw.cloudatlas.modules;

public class MNUMessage extends Message {
    Message msg;
    MNUMessage(Message m) {
        msg = m;
        source = m.destination;
        destination = m.source;
        type = msgType.MsgNotUnderstood;
    }
}
