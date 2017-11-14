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

    // TODO should getManagedZones return Set<PathName>
    @Override
    public Set<ZMI> getManagedZones() {
        Set<ZMI> managedZones = new HashSet<>();
        ZMI zmi = zone;
        while (zmi.getFather() != null) {
            managedZones.addAll(zmi.getSons());
            zmi = zmi.getFather();
        }
        managedZones.add(zmi);
        return managedZones;
    }

    @Override
    public AttributesMap getValues(PathName zone) {
        ZMI zmi = getZMI(zone);
        if (zmi == null) return new AttributesMap();
        return zmi.getAttributes();
    }

    @Override
    public void installQuery(String query) {
        // TODO interpreter

    }

    @Override
    public void uninstallQuery(String queryName) {
        // TODO interpreter
    }

    @Override
    public void setValues(PathName zone, AttributesMap attributes) {
        ZMI zmi = getZMI(zone);
        if (zmi == null || !zmi.getSons().isEmpty()) return;
        AttributesMap zoneAttributes = zmi.getAttributes();
        for (Map.Entry<Attribute, Value> entry : attributes) {
            if (zoneAttributes.getOrNull(entry.getKey()) != null) {
                zoneAttributes.addOrChange(entry);
            }
        }
    }

    @Override
    public void setValues(AttributesMap attributes) {
        setValues(getPathName(zone), attributes);
    }

    @Override
    public void setContacts(Set<ValueContact> contacts) {
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
