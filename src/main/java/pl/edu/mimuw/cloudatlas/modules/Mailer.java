package pl.edu.mimuw.cloudatlas.modules;


import static pl.edu.mimuw.cloudatlas.modules.sendError.OK;

enum sendError {
    IDK, OK
}
//TODO chyba nie będzie potrzebne
public class Mailer extends Module {
    private static final Mailer INSTANCE = new Mailer();

    public static Mailer getInstance() {
        return INSTANCE;
    }


    synchronized sendError passMessage(Message m) {
        sendMessage(m);
        return OK;
    }

    @Override
    public void handleMsg(Message msg) {
        msg.destination.sendMessage(msg);
    }


}
