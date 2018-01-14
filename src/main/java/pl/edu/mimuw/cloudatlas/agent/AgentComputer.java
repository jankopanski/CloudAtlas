package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.*;
import pl.edu.mimuw.cloudatlas.modules.gossip.GossipModule;
import pl.edu.mimuw.cloudatlas.modules.gossip.RandomStrategy;
import pl.edu.mimuw.cloudatlas.security.InvalidQueryException;
import pl.edu.mimuw.cloudatlas.security.Signature;
import pl.edu.mimuw.cloudatlas.security.SignatureChecker;

import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.Duration;
import java.util.*;

public class AgentComputer extends Module implements Agent {
    private static AgentComputer INSTANCE = new AgentComputer();
    private static SignatureChecker signatureChecker;

    public static AgentComputer getInstance() {
        return INSTANCE;
    }

    private ZMI root, zone;
    private Set<ValueContact> contacts = new HashSet<>();

    private AgentComputer() {
        new Thread(() -> this.runModule()).start();

    }

    public static void initialize(ZMI zone, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        INSTANCE.zone = zone;
        INSTANCE.root = zone;
        while (INSTANCE.root.getFather() != null)
            INSTANCE.root = INSTANCE.root.getFather();
        signatureChecker = new SignatureChecker(publicKey);
        GossipModule gm = GossipModule.getInstance();
        gm.initialize(Duration.ofSeconds(1), Duration.ofSeconds(1), new RandomStrategy(2, 1), zone, new PathName("/uw/violet07"), 10);
        CommunicationModule cm = CommunicationModule.getInstance();
        cm.setDstModule(gm);
        cm.setNodeNameAndPorts("/uw/violet07", 1234, 1234);
        GossipModule.getInstance().startModule();
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.type) {
            case RMICall:
                execMethod((RMIMessage) msg);
                break;
            default: super.handleMsg(msg);
        }
    }

    public ZMI getZone() {
        return zone;
    }

    private void execMethod(RMIMessage msg) {
        Object res = null;
        switch (msg.method) {
            case getManagedZones:
                res = getManagedZones();
                break;
            case getValues:
                res = getValues(((PathName) msg.params.get(0)));
                break;
            case installQuery:
                res = installQuery((PathName) msg.params.get(0), (String) msg.params.get(1), (Signature) msg.params.get(2));
                break;
            case uninstallQuery:
                res = uninstallQuery((PathName) msg.params.get(0), (String) msg.params.get(1));
                break;
            case setValues:
                setValues((PathName) msg.params.get(0), (AttributesMap) msg.params.get(1));
                break;
            case setValuesDefaultZone:
                setValues((AttributesMap) msg.params.get(0));
                break;
            case setContacts:
                setContacts((Set<ValueContact>) msg.params.get(0));
                break;
        }
        if (msg.ret) {
            RMIModule rmi = RMIModule.getInstance();
            RMIReturnMessage retmsg = new RMIReturnMessage();
            retmsg.destination = msg.source;
            retmsg.source = msg.destination;
            retmsg.method = msg.method;
            retmsg.returnValue = res;
            rmi.sendMessage(retmsg);
        }
//        System.err.println("AgentComputer execMethod exit");
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


    public synchronized List<QueryResult> executeQueries(ZMI zmi, String query) {
        if (zmi == null)
            return new ArrayList<>();
        else {
            List<QueryResult> ancestorsResults = executeQueries(zmi.getFather(), query);
            if (!zmi.getSons().isEmpty()) {
                Interpreter interpreter = new Interpreter(zmi);
                Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
                try {
                    List<QueryResult> result = interpreter.interpretProgram((new parser(lex)).pProgram());
                    for (QueryResult r : result) {
                        zmi.getAttributes().addOrChange(r.getName(), r.getValue());
                    }
                    System.out.println(query);
                    ancestorsResults.addAll(result);
                } catch (Exception exception) {
                }
            }
            return ancestorsResults;
        }
    }

    @Override
    public synchronized Boolean installQuery(PathName zone, String query, Signature signature) {
        try {
            if (!signatureChecker.check(query, signature)) {
                return false;
            }
        } catch (InvalidQueryException e) {
            return false;
        }

        ZMI zmi;
        System.out.println(query);
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
            if (result.isEmpty())
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
    public synchronized Boolean uninstallQuery(PathName zone, String queryName) {

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

    public void updateQueries() {
        ZMI zmi = zone;
        if (zone != null)
            do {
                for (Map.Entry<Attribute, Value> e : zmi.getAttributes()) {
                    if (Attribute.isQuery(e.getKey())) {
                        ValueCert cert = (ValueCert) e.getValue();
                        executeQueries(zmi, cert.getQuery());
                    }
                }
                zmi.getAttributes().addOrChange("timestamp", new ValueTime(System.currentTimeMillis()));
                zmi = zmi.getFather();
            } while (zmi != null);
        else
            System.out.println("zone not yet initialized");
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
