package pl.edu.mimuw.cloudatlas.fetcher;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;

public interface Fetcher extends Remote {
    public AttributesMap fetch() throws RemoteException;
}
