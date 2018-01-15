package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueCert;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.modules.RMIModule;
import pl.edu.mimuw.cloudatlas.security.KeyReader;
import pl.edu.mimuw.cloudatlas.security.KeyReaderException;

import java.io.IOException;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.Map;

public class Main {
    private static ZMI root, zone;
    private static String host;
    private static int port;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: Main <public_key> <config_file>");
            System.exit(1);
        }

        try {
            ZMICreator.createHierarchy(args[1]);
            root = ZMICreator.getRoot();
            zone = ZMICreator.getZone();
            host = ZMICreator.getHost();
            port = ZMICreator.getPort();

        } catch (IOException | org.json.simple.parser.ParseException | InvalidConfigException | ParseException e) {
            e.printStackTrace();
        }

        String publicKeyFile = args[0];
        PublicKey publicKey = null;
        try {
            publicKey = KeyReader.readPublicKey(publicKeyFile);
        } catch (KeyReaderException e) {
            System.err.println("Public key read error");
            e.printStackTrace();
            System.exit(1);
        }

        AgentComputer agent = AgentComputer.getInstance();
        try {
            AgentComputer.initialize(zone, publicKey);
        }
        catch (Exception e) {
            System.err.println("Agent initialization error");
            e.printStackTrace();
            System.exit(1);
        }
        RMIModule rmi = RMIModule.getInstance();


        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    updateQueries(root);
                    try {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e) {

                    }
                }
            }
            private void updateQueries(ZMI zmi) {
                for (ZMI son : zmi.getSons())
                    updateQueries(son);
                for (Map.Entry<Attribute, Value> e : zmi.getAttributes()) {
                    if (Attribute.isQuery(e.getKey())) {
                        ValueCert cert = (ValueCert) e.getValue();
                        agent.executeQueries(zmi, cert.getQuery());
                    }
                }
            }

        };

        new Thread(r).start();

        AgentServer server = new AgentServer(rmi, host, port);
        server.run();
    }
}
