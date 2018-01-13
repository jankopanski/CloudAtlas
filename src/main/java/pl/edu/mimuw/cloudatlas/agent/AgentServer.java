package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AgentServer implements Runnable {
    private String registryHost;
    private int registryPort;
    private Agent agent;

    public AgentServer(Agent agent, String host, int port) {
        this.registryHost = host;
        this.registryPort = port;
        this.agent = agent;
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }


    @Override
    public void run() {
        try {
            Agent stub = (Agent) UnicastRemoteObject.exportObject(agent, 0);
            Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
            registry.rebind(Agent.NAME, stub);
        } catch (RemoteException e) {
            System.err.println("RMI server error");
            e.printStackTrace();
        }
    }
}
