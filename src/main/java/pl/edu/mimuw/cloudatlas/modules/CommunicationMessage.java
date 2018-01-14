package pl.edu.mimuw.cloudatlas.modules;

import lombok.NoArgsConstructor;

import java.net.InetAddress;

@NoArgsConstructor
public class CommunicationMessage extends Message {
    InetAddress IP;
    String data;

    public CommunicationMessage(Module src, InetAddress addr, String dat) {
        super(src, CommunicationModule.getInstance(), msgType.Communication);
        IP = addr;
        data = dat;
    }
}
