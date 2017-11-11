package pl.edu.mimuw.cloudatlas.fetcher;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import org.ini4j.Ini;
import pl.edu.mimuw.cloudatlas.model.Attribute;

import static java.lang.Boolean.parseBoolean;

public class FetcherServer {
    private static final String CONFIG_SECTION = "attributes";
    private static final int PORT = 0;

    public static void main(String[] args) {
        System.out.println("It works!");
        if (args.length != 1) {
            System.err.println("Usage: java FetcherServer <config_file>");
            System.exit(1);
        }
        try {
            Collection<Attribute> attributes = readConfig(args[0]);
            AttributeFetcher object = new AttributeFetcher(attributes);
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            Fetcher stub = (Fetcher) UnicastRemoteObject.exportObject(object, PORT);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Fetcher", stub);
        } catch (IOException e) {
            System.err.println("Config file not found");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("RMI server error");
            e.printStackTrace();
        }
    }

    private static Collection<Attribute> readConfig(String filename) throws IOException {
        Ini ini = new Ini(new File(filename));
        Map<String, String> attrMap = ini.get(CONFIG_SECTION);
        Collection<Attribute> attributes= new LinkedList<Attribute>();
        attrMap.forEach((String key, String value) -> {
            if (parseBoolean(value))
                attributes.add(new Attribute(key));
        });
        return attributes;
    }
}
