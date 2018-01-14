package pl.edu.mimuw.cloudatlas.modules;

import lombok.Data;

import java.time.LocalTime;
import java.util.function.Function;

@Data
public class TimerMessage extends Message implements Comparable<TimerMessage> {
    LocalTime fireTime;
    Runnable callback;

    public TimerMessage(Module src, LocalTime ft, Runnable cb) {
        super(src, TimerModule.getInstance(), msgType.Timer);
        fireTime = ft;
        callback = cb;
    }

    public int compareTo(TimerMessage msg) {
        return this.fireTime.compareTo(msg.fireTime);
    }

}
