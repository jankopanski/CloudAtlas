package pl.edu.mimuw.cloudatlas.modules;

import java.io.ObjectInputStream;

public class TimerResponse extends Message {
    public int timerId;

    public TimerResponse(Module source, int tId) {
        super(source, TimerModule.getInstance(), msgType.TimerResponse);
        timerId = tId;
    }

    public boolean equals(TimerResponse r) {
        return timerId == r.timerId;
    }
}
