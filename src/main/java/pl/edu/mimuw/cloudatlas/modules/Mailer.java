package pl.edu.mimuw.cloudatlas.modules;


import static pl.edu.mimuw.cloudatlas.modules.sendError.OK;

enum sendError {
    IDK, OK
}
//TODO chyba nie będzie potrzebne
public class Mailer extends Module {
    synchronized static sendError passMessage(Message m) {
        sendMessage(m);
        return OK;
    }

    @Override
    void handleMsg(Message msg) {
        msg.destination.sendMessage(msg);
    }


}
