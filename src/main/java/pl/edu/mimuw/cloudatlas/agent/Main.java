package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.modules.RMIModule;
import pl.edu.mimuw.cloudatlas.modules.gossip.GossipConfig;
import pl.edu.mimuw.cloudatlas.security.KeyReader;
import pl.edu.mimuw.cloudatlas.security.KeyReaderException;

import java.io.IOException;
import java.security.PublicKey;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: Main <public_key> <config_file>");
            System.exit(1);
        }

        try {
            ZMICreator.createHierarchy(args[1]);

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
            AgentComputer.initialize(ZMICreator.getZone(), publicKey, ZMICreator.getGossipConfig());
        }
        catch (Exception e) {
            System.err.println("Agent initialization error");
            e.printStackTrace();
            System.exit(1);
        }
        RMIModule rmi = RMIModule.getInstance();

        AgentServer server = new AgentServer(rmi, ZMICreator.getHost(), ZMICreator.getPort());
        server.run();
    }
}
