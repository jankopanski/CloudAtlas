package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class FetcherClient {
    private static final String FETCHER_HOST = "localhost";
    private Registry registry = null;
    private Fetcher stub = null;

    public FetcherClient() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
    }

    public AttributesMap fetchInfo () throws RemoteException, NotBoundException {
        try {
            if (registry == null || stub == null) {
                registry = LocateRegistry.getRegistry(FETCHER_HOST);
                stub = (Fetcher) registry.lookup("Fetcher");
            }
            return stub.fetch();
        } catch (Exception e) {
            registry = null;
            stub = null;
            System.err.println("Fetch error: " + e.getMessage());
            throw e;
        }
    }
}
