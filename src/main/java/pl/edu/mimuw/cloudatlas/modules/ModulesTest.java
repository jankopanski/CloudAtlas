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

    private static class CommTestMdl extends Module {
       @Override
       public void handleMsg(Message msg) {
           if (msg.type == msgType.Communication) {
               CommunicationMessage cMsg = (CommunicationMessage) msg;
               System.out.println(cMsg.data);
               System.out.println(cMsg.data.length());
               System.out.println(cMsg.IP);
               try {sleep(2000);} catch (Exception e) {e.printStackTrace();}
               Module tmp = cMsg.destination;
               cMsg.destination = cMsg.source;
               cMsg.source = tmp;
               tmp.sendMessage(cMsg);
           }
           else {
               System.out.println("Wrong message type");
           }
       }

       CommTestMdl() {
           new Thread(() -> {this.runModule();}).start();
       }
    }

    public static void commSimpleTest() {
        CommunicationModule m1, m2;
        CommTestMdl tMdl = new CommTestMdl();
        m1 = new CommunicationModule();
        m1.setNodeNameAndPorts("a", 1234, 5678);
        m1.setDstModule(tMdl);
        //m2 = new CommunicationModule();
        //m2.setNodeNameAndPorts("a", 5678, 1234);
        //m2.setDstModule(tMdl);
        CommunicationMessage msg1 = new CommunicationMessage();
        CommunicationMessage msg2 = new CommunicationMessage();
        msg1.destination = m1;
        msg2.destination = m1;
        msg1.source = null;
        msg1.type = msgType.Communication;
        msg2.type = msgType.Communication;
        try {
            msg1.IP = InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, 0, (byte)80});
            msg2.IP = InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, 0, (byte)80});
            msg1.data = "Hello world";
            m1.sendMessage(msg1);
            String longMsg = "aaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbababababababababab";
            msg2.data = longMsg;
            System.out.println(longMsg.length());
            m1.sendMessage(msg2);
            m1.sendMessage(msg1);
        } catch (Exception e) {}
    }
}
