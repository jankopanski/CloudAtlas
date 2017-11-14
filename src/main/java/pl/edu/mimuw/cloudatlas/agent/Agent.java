package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.rmi.Remote;
import java.util.Set;

public interface Agent extends Remote {
    /** Returns the set of zones on which the agent stores information. */
    public Set<ZMI> getManagedZones();

    /** Returns the values of attributes of a given zone. */
    public AttributesMap getValues(PathName zone);

    /** Installs a query on the agent. We assume that the query is installed in all zones of the agent. */
    public void installQuery(String query);

    /** Uninstalls a query on the agent. Again, the query is uninstalled from all zones of the agent. */
    public void uninstallQuery(String queryName);

    /** Sets the values of attributes of a given zone. */
    public void setValues(PathName zone, AttributesMap attributes);

    /** Sets the values of attributes of agent associated zone. */
    public void setValues(AttributesMap attributes);

    /** Sets the fallback contacts. */
    public void setContacts(Set<ValueContact> contacts);
}
