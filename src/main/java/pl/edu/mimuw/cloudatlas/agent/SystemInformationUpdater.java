package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.fetcher.FetcherClient;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SystemInformationUpdater implements Runnable {
    private static final long FETCH_INTERVAL = 1000;
    private Agent agent;
    private FetcherClient fetcher;

    public SystemInformationUpdater(Agent agent) {
        this.agent = agent;
        fetcher = new FetcherClient();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(FETCH_INTERVAL);
                AttributesMap info = fetcher.fetchInfo();
                agent.setValues(info);
            } catch (InterruptedException e) {
                break;
            } catch (RemoteException | NotBoundException e) {
                System.err.println("Fetch error: " + e.getMessage());
            }
        }
    }
}
