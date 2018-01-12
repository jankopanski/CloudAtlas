package pl.edu.mimuw.cloudatlas.modules;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.LocalTime;

import static java.lang.Thread.sleep;

public class ModulesTest {
    public static void main(String args[]) {
        //timerSimpleTest();
        commSimpleTest();
    }


    public static void timerSimpleTest() {
        for (int i = 0; i < 5; ++i) {
            TimerModule timer = TimerModule.getInstance();
            TimerMessage msg = new TimerMessage();
            msg.destination = timer;
            msg.source = null;
            msg.type = msgType.Timer;
            msg.fireTime = LocalTime.now().plusSeconds(5);
            msg.callback = () -> {
                System.out.println("hello world");
            };
            timer.sendMessage(msg);
            try {sleep(5000);} catch (InterruptedException e) {};
            System.out.println("Hello time");
        }
        System.out.println("timer simple test done");
    }

    public static void commSimpleTest() {
        CommunicationModule m1, m2;
        m1 = new CommunicationModule();
        m1.setNodeNameAndPorts("a", 1234, 5678);
        m2 = new CommunicationModule();
        m2.setNodeNameAndPorts("a", 5678, 1234);
        CommunicationMessage msg = new CommunicationMessage();
        msg.destination = m1;
        msg.source = null;
        msg.type = msgType.Comunication;
        try {
            msg.destIP = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
            msg.data = "Hello world";
            m1.sendMessage(msg);

        } catch (Exception e) {}
    }
}
