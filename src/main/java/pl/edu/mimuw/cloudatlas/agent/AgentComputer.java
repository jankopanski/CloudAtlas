package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AgentComputer implements Agent {
    private ZMI root, zone;
    private Set<ValueContact> contacts;

    public AgentComputer(ZMI zone) {
        this.zone = zone;
        contacts = new HashSet<>();
        root = zone;
        while (root.getFather() != null)
            root = root.getFather();
    }

    @Override
    public synchronized Set<PathName> getManagedZones() {
        Set<PathName> managedZones = new HashSet<>();
        ZMI zmi = zone;
        while (zmi.getFather() != null) {
            for (ZMI son : zmi.getSons())
                managedZones.add(getPathName(son));
            zmi = zmi.getFather();
        }
        managedZones.add(getPathName(zmi));
        return managedZones;
    }

    @Override
    public synchronized AttributesMap getValues(PathName zone) {
        ZMI zmi = getZMI(zone);
        if (zmi == null) return new AttributesMap();
        return zmi.getAttributes();
    }

    @Override
    public synchronized void installQuery(String query) {
        // TODO interpreter
//        executeQuery()
        // code to erase
        for (Map.Entry<Attribute, Value> entry: zone.getAttributes()) {
            System.out.println(entry.getKey().getName() + ": " + entry.getValue().toString());
        }
    }

    @Override
    public synchronized void uninstallQuery(String queryName) {
        // TODO interpreter
    }

    @Override
    public synchronized void setValues(PathName zone, AttributesMap attributes) {
        ZMI zmi = getZMI(zone);
        if (zmi == null || !zmi.getSons().isEmpty()) return;
        zmi.getAttributes().addOrChange(attributes);
        // TODO Should setValues add new attributes or only modify existing attributes
//        AttributesMap zoneAttributes = zmi.getAttributes();
//        for (Map.Entry<Attribute, Value> entry : attributes) {
//            if (zoneAttributes.getOrNull(entry.getKey()) != null) {
//                zoneAttributes.addOrChange(entry);
//            }
//        }
    }

    @Override
    public synchronized void setValues(AttributesMap attributes) {
        setValues(getPathName(zone), attributes);
    }

    @Override
    public synchronized void setContacts(Set<ValueContact> contacts) {
        this.contacts = contacts;
    }

    private static String getName(ZMI zmi) {
        return ((ValueString)zmi.getAttributes().get("name")).getValue();
//        Value name = zmi.getAttributes().get("name");
//        if (name.getType() != TypePrimitive.STRING)
//            return null;
//        return ((ValueString) name).getValue();
    }

    private static PathName getPathName(ZMI zmi) {
        String name = ((ValueString)zmi.getAttributes().get("name")).getValue();
        return zmi.getFather() == null? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
    }

    private ZMI getZMI(PathName zone) {
        ZMI zmi = root;
        for (String component : zone.getComponents()) {
            Boolean found = false;
            for (ZMI son : zmi.getSons()) {
                String sonName = getName(son);
                if (component.equals(sonName)) {
                    zmi = son;
                    found = true;
                    break;
                }
            }
            if (!found) return null;
        }
        return zmi;
    }
}
