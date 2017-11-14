package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Agent extends Remote {
    /** Returns the set of zones on which the agent stores information. */
    public Set<PathName> getManagedZones() throws RemoteException;

    /** Returns the values of attributes of a given zone. */
    public AttributesMap getValues(PathName zone) throws RemoteException;

    /** Installs a query on the agent. We assume that the query is installed in all zones of the agent. */
    public void installQuery(String query) throws RemoteException;

    /** Uninstalls a query on the agent. Again, the query is uninstalled from all zones of the agent. */
    public void uninstallQuery(String queryName) throws RemoteException;

    /** Sets the values of attributes of a given zone. */
    public void setValues(PathName zone, AttributesMap attributes) throws RemoteException;

    /** Sets the values of attributes of agent associated zone. */
    public void setValues(AttributesMap attributes) throws RemoteException;

    /** Sets the fallback contacts. */
    public void setContacts(Set<ValueContact> contacts) throws RemoteException;
}
