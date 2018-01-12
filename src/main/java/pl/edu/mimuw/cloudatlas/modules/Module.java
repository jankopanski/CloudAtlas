package pl.edu.mimuw.cloudatlas.modules;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Module {
    BlockingQueue<Message> msgQ = new LinkedBlockingQueue<>();


    protected Message getMessage() {
        try {
            return msgQ.take();
        } catch (InterruptedException e) {return null;}
    }


    public synchronized void sendMessage(Message msg) {
        msgQ.add(msg);
    }

    public void runModule() {
        while (true) {
            Message msg = getMessage();
            handleMsg(msg);
        }
    }

    public void handleMsg(Message msg) {
        switch (msg.type) {
            default:
                msg.source.sendMessage(new MNUMessage(msg));
        }
    }

}
