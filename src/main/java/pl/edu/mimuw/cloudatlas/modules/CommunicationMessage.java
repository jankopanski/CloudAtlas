package pl.edu.mimuw.cloudatlas.modules;

import lombok.NoArgsConstructor;

import java.net.InetAddress;

@NoArgsConstructor
public class CommunicationMessage extends Message {
    public InetAddress IP;
    public byte[] data;

    public CommunicationMessage(Module src, InetAddress addr, byte[] dat) {
        super(src, CommunicationModule.getInstance(), msgType.Communication);
        IP = addr;
        data = dat;
    }
}
