package pl.edu.mimuw.cloudatlas.modules;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.PriorityBlockingQueue;

import static java.lang.Thread.sleep;

public class TimerModule extends Module {
    private static final TimerModule INSTANCE = new TimerModule();

    private PriorityBlockingQueue<TimerMessage> sleeperQ = new PriorityBlockingQueue<>();

    private Thread sleeper = new Thread(
            () -> {
                synchronized(this) {
                    while (true) {
                        TimerMessage msg = sleeperQ.peek();
                        LocalTime now = LocalTime.now();
                        if (msg != null) {
                            if (msg.fireTime.isBefore(now)) {
                                msg = sleeperQ.poll();
                                msg.callback.run();
                            } else try {
                                wait(now.until(msg.fireTime, ChronoUnit.MILLIS));
                            } catch (InterruptedException e) {
                            }
                        } else
                            try {
                                wait();
                            } catch (InterruptedException e) {
                            }
                    }
                }
            }

    );

    public static TimerModule getInstance() {
        return INSTANCE;
    }

    private TimerModule() {
        sleeper.start();
        new Thread(() -> this.runModule()).start();
    }

    @Override
    synchronized void handleMsg(Message msg) {
        switch (msg.type) {
            case Timer:
                sleeperQ.add((TimerMessage) msg);
                notify();
                break;
            default: super.handleMsg(msg);
        }
    }



}
