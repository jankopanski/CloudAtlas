package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.agent.AgentComputer;
import pl.edu.mimuw.cloudatlas.agent.AgentMethod;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.security.Signature;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RMIModule extends Module implements Agent, Runnable {
    private static final RMIModule INSTANCE = new RMIModule();
    private static final AgentComputer agent = AgentComputer.getInstance();
    private static Map<AgentMethod, ReturnValue> locks;

    private RMIModule() {
        locks = new HashMap<>();
        for (AgentMethod m : AgentMethod.values()) {
            locks.put(m, new ReturnValue());
        }
        new Thread(() -> this.runModule()).start();
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
        ReturnValue val = locks.get(AgentMethod.getManagedZones);
        synchronized (val) {
            val.wasCallback = false;
            agent.sendMessage(msg);
            while (!val.wasCallback) {
                try {
                    val.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return (Set<PathName>) val.value;
    }

    @Override
    public AttributesMap getValues(PathName zone) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.getValues;
        msg.params = new ArrayList<>();
        msg.params.add(zone);
        msg.ret = true;
        ReturnValue val = locks.get(AgentMethod.getValues);
        synchronized (val) {
            val.wasCallback = false;
            agent.sendMessage(msg);
            while (!val.wasCallback) {
                try {
                    val.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return (AttributesMap) val.value;
    }

    @Override
    public Boolean installQuery(PathName zone, String query, Signature signature) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.destination = AgentComputer.getInstance();
        msg.source = this;
        msg.method = AgentMethod.installQuery;
        msg.params = new ArrayList<>();
        msg.params.add(zone);
        msg.params.add(query);
        msg.params.add(signature);
        msg.ret = true;
        ReturnValue val = locks.get(AgentMethod.installQuery);
        synchronized (val) {
            val.wasCallback = false;
            agent.sendMessage(msg);
            while (!val.wasCallback) {
                try {
                    val.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return (Boolean) val.value;
    }

    @Override
    public Boolean uninstallQuery(PathName zone, String queryName) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.uninstallQuery;
        msg.params = new ArrayList<>();
        msg.params.add(zone);
        msg.params.add(queryName);
        msg.ret = true;
        ReturnValue val = locks.get(AgentMethod.uninstallQuery);
        synchronized (val) {
            val.wasCallback = false;
            agent.sendMessage(msg);
            while (!val.wasCallback) {
                try {
                    val.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return (Boolean) val.value;
    }

    @Override
    public void setValues(PathName zone, AttributesMap attributes) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.setValues;
        msg.params = new ArrayList<>();
        msg.params.add(zone);
        msg.params.add(attributes);
        agent.sendMessage(msg);
    }

    @Override
    public void setValues(AttributesMap attributes) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.setValuesDefaultZone;
        msg.params = new ArrayList<>();
        msg.params.add(attributes);
        agent.sendMessage(msg);
    }

    @Override
    public void setContacts(Set<ValueContact> contacts) throws RemoteException {
        RMIMessage msg = new RMIMessage();
        msg.method = AgentMethod.setContacts;
        msg.params = new ArrayList<>();
        msg.params.add(contacts);
        agent.sendMessage(msg);
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.type) {
            case RMICallback:
                RMIReturnMessage rmimsg = (RMIReturnMessage) msg;
                ReturnValue lock = locks.get(rmimsg.method);
                synchronized (lock) {
                    lock.value = rmimsg.returnValue;
                    lock.wasCallback = true;
                    lock.notify();
                }
                break;
            default: super.handleMsg(msg);
        }
    }

    @Override
    public void run() {
        runModule();
    }

    private class ReturnValue {
        Object value;
        boolean wasCallback;
    }
}
