package pl.edu.mimuw.cloudatlas.fetcher;

import org.ini4j.Ini;
import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.parseBoolean;

public class Fetcher {
    private static final String AGENT_HOST = "localhost";
    private static final String GENERAL_SECTION = "general";
    private static final String ATTRIBUTES_SECTION = "attributes";
    private static int agent_port;
    private static int collectionInterval = 1000;
    private static int averagingPeriod = 5000;
    private static String averagingMethod = "none";

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: ./fetcher.sh <registry_port> ");
            System.exit(1);
        }
        else {
            agent_port = Integer.parseInt(args[1]);
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        SystemInformationCollector collector = new SystemInformationCollector(readConfig(args[0]));
        AverageCounter averageCounter = new AverageCounter(averagingMethod, collectionInterval, averagingPeriod);
        Registry registry = null;
        Agent agent = null;
        while (true) {
            averageCounter.add(collector.collect());
            AttributesMap attributesMap = averageCounter.get();
            try {
                if (registry == null || agent == null) {
                    registry = LocateRegistry.getRegistry(AGENT_HOST, agent_port);
                    agent = (Agent) registry.lookup(Agent.NAME);
                }
                agent.setValues(attributesMap);
            } catch (RemoteException | NotBoundException e) {
                registry = null;
                agent = null;
                System.err.println(e.getMessage());
            }
            try {
                TimeUnit.MILLISECONDS.sleep(collectionInterval);
            } catch (InterruptedException e) {
                System.out.println("Interrupted, finishing fetching");
                break;
            }
        }
    }

    private static Collection<Attribute> readConfig(String filename) throws IOException {
        Ini ini = new Ini(new File(filename));
        collectionInterval = Integer.parseInt(ini.get(GENERAL_SECTION, "interval"));
        averagingPeriod = Integer.parseInt(ini.get(GENERAL_SECTION, "period"));
        averagingMethod = ini.get(GENERAL_SECTION, "method");
        Map<String, String> attrMap = ini.get(ATTRIBUTES_SECTION);
        Collection<Attribute> attributes= new LinkedList<>();
        attrMap.forEach((String key, String value) -> {
            if (parseBoolean(value))
                attributes.add(new Attribute(key));
        });
        return attributes;
    }
}
