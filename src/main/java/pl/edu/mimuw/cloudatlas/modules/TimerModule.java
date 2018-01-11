package pl.edu.mimuw.cloudatlas.modules;

public class TimerModule extends Module {
    private static final TimerModule INSTANCE = new TimerModule();

    public static TimerModule getInstance() {
        return INSTANCE;
    }
}
