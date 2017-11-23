package pl.edu.mimuw.cloudatlas.agent;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.w3c.dom.Attr;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.ByteArrayInputStream;
import java.util.*;

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

    private Set<PathName> getManagedZonesHelper(ZMI zmi) {
        Set<PathName> managedZones = new HashSet<>();
        for (ZMI son : zmi.getSons()) {
            managedZones.addAll(getManagedZonesHelper(son));
        }
        managedZones.add(getPathName(zmi));
        return managedZones;
    }

    @Override
    public synchronized Set<PathName> getManagedZones() {
       return getManagedZonesHelper(root);
    }

    @Override
    public synchronized AttributesMap getValues(PathName zone) {
        ZMI zmi = getZMI(zone);
        if (zmi == null) return new AttributesMap();
        return zmi.getAttributes();
    }

    public List<QueryResult> executeQueries(ZMI zmi, String query) {
        if(!zmi.getSons().isEmpty()) {

            for(ZMI son : zmi.getSons())
                executeQueries(son, query);
            Interpreter interpreter = new Interpreter(zmi);
            Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
            try {
                List<QueryResult> result = interpreter.interpretProgram((new parser(lex)).pProgram());
                if (result.isEmpty())
                    return null;
                for (QueryResult r : result) {
                    zmi.getAttributes().addOrChange(r.getName(), r.getValue());
                }
                System.out.println(query);
                return result;

            } catch (Exception exception) {
                return null;
            }
        }
        else
            return null;
    }

    @Override
    public synchronized boolean installQuery(PathName zone, String query) {

        ZMI zmi;

        if (zone == null)
            zmi = root;
        else
            zmi = getZMI(zone);

        String[] splitted = query.split(":");
        List<String> names = new ArrayList<>();
        List<String> queries = new ArrayList<>();

        names.add(splitted[0].trim());

        for (int i = 1; i < splitted.length - 1; ++i) {
            int splitPoint = splitted[i].lastIndexOf("\n") + 1;
            names.add(splitted[i].substring(splitPoint).trim());
            queries.add(splitted[i].substring(0, splitPoint).trim());
        }

        queries.add(splitted[splitted.length - 1].trim());

        for (int i = 0; i < names.size(); ++i) {
            List<QueryResult> result = executeQueries(zmi, queries.get(i));
            if (result == null)
                return false;
            Attribute certName = new Attribute(names.get(i));
            ValueCert cert = new ValueCert(certName, queries.get(i), new ArrayList<>());

            for (QueryResult r : result)
                cert.getAttributes().add(r.getName());

            cert.getAttributes().add(certName);
            zmi.getAttributes().addOrChange(certName, cert);
        }
        return true;
    }

    public static void removeCert(ZMI zmi, String queryName) {
        if (!zmi.getSons().isEmpty()) {
            for (ZMI son : zmi.getSons()) {
                removeCert(son, queryName);
            }
            ValueCert cert = (ValueCert) zmi.getAttributes().getOrNull(queryName);
            if (cert != null) {
                for (Attribute a : cert.getAttributes())
                    zmi.getAttributes().remove(a);
            }

        }
    }

    @Override
    public synchronized boolean uninstallQuery(PathName zone, String queryName) {

        if (!queryName.startsWith("&"))
            return false;

        ZMI zmi = getZMI(zone);

        try {
            removeCert(zmi, queryName);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
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
        System.out.println(contacts);
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
