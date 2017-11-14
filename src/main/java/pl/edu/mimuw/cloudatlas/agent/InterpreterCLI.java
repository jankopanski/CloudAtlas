package pl.edu.mimuw.cloudatlas.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InterpreterCLI {
    private static final String AGENT_HOST = "localhost";

    public static void main(String[] args) throws IOException, NotBoundException {
        Agent agent = initializeAgent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String query = bufferedReader.readLine();
        agent.installQuery(query);
    }

    private static Agent initializeAgent() throws RemoteException, NotBoundException {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        Registry registry = LocateRegistry.getRegistry(AGENT_HOST);
        return (Agent) registry.lookup("Agent");
    }
}
