package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AgentServer implements Runnable {
    private static final int PORT = 0;
    private Agent agent;

    public AgentServer(Agent agent) {
        this.agent = agent;
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }


    @Override
    public void run() {
        try {
            Agent stub = (Agent) UnicastRemoteObject.exportObject(agent, PORT);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Agent", stub);
        } catch (RemoteException e) {
            System.err.println("RMI server error");
            e.printStackTrace();
        }
    }
}
