package pl.edu.mimuw.cloudatlas.modules;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Module {
    static BlockingQueue<Message> msgQ = new LinkedBlockingQueue<>();

    protected static Message getMessage() {
        try {
            return msgQ.take();
        } catch (InterruptedException e) {return null;}
    }


    protected static synchronized void sendMessage(Message msg) {
        msgQ.add(msg);
    }

    public void runModule() {
        while (true) {
            Message msg = getMessage();
            handleMsg(msg);
        }
    }

    void handleMsg(Message msg) {
        switch (msg.type) {
            default:
                msg.source.sendMessage(new MNUMessage(msg));
        }
    }

}
