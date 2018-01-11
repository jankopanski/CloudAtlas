package pl.edu.mimuw.cloudatlas.modules;

import java.time.LocalTime;
import java.util.function.Function;

public class TimerMessage extends Message implements Comparable<TimerMessage> {
    LocalTime fireTime;
    Runnable callback;

    public int compareTo(TimerMessage msg) {
        return this.fireTime.compareTo(msg.fireTime);
    }

}
