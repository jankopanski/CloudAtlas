package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.agent.AgentComputer;
import pl.edu.mimuw.cloudatlas.agent.AgentMethod;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class RMIModule extends Module implements Agent {
    private static final RMIModule INSTANCE = new RMIModule();
    private static final AgentComputer agent = AgentComputer.getInstance();
    private static Map<AgentMethod, ReturnValue> locks;

    private RMIModule() {
        for (AgentMethod m : AgentMethod.values()) {
            locks.put(m, new ReturnValue());
        }
        // TODO dodać Thread z runModule
    }

    public static RMIModule getInstance() {
        return INSTANCE;
    }

    @Override
    public Set<PathName> getManagedZones() throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.getManagedZones;
        msg.params = new ArrayList<>();
        msg.ret = true;
        agent.sendMessage(msg);
        ReturnValue val = locks.get(AgentMethod.getManagedZones);
        synchronized (val) {
            try {
                val.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (Set<PathName>) val.value;
    }

    @Override
    public AttributesMap getValues(PathName zone) throws RemoteException {
        return null;
    }

    @Override
    public Boolean installQuery(PathName zone, String query) throws RemoteException {
        return false;
    }

    @Override
    public Boolean uninstallQuery(PathName zone, String queryName) throws RemoteException {
        return false;
    }

    @Override
    public void setValues(PathName zone, AttributesMap attributes) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.setValues;
        msg.params = new ArrayList<>();
        msg.params.add(zone);
        msg.params.add(attributes);
        msg.ret = true;
        agent.sendMessage(msg);
    }

    @Override
    public void setValues(AttributesMap attributes) throws RemoteException {

    }

    @Override
    public void setContacts(Set<ValueContact> contacts) throws RemoteException {

    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.type) {
            case RMICall:
                RMIReturnMessage rmimsg = (RMIReturnMessage) msg;
                ReturnValue lock = locks.get(rmimsg.method);
                lock.value = rmimsg.returnValue;
                lock.notify();
                break;
            default: super.handleMsg(msg);
        }
    }

    private class ReturnValue {
        Object value;
    }
}
