package pl.edu.mimuw.cloudatlas.modules;

import java.time.LocalTime;

import static java.lang.Thread.sleep;

public class ModulesTest {
    public static void main(String args[]) {
        timerSimpleTest();
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
}
