package pl.edu.mimuw.cloudatlas.security;

import javax.crypto.NoSuchPaddingException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;


public class QuerySignerServer {
    private static String privateKeyFile;
    private static int registryPort;


    public static void main(String args[]) throws KeyReaderException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, RemoteException {
        if (args.length == 2) {
            privateKeyFile = args[0];
            registryPort = Integer.parseInt(args[1]);
        }
        else {
            System.err.println("Usage: QuerySignerServer <private_key> <registry_port>");
            System.exit(1);
        }


        PrivateKey privateKey = KeyReader.readPrivateKey(privateKeyFile);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        QuerySignerComputer object = new QuerySignerComputer(privateKey);
        QuerySigner stub = (QuerySigner) UnicastRemoteObject.exportObject(object, 0);
        Registry registry = LocateRegistry.getRegistry("localhost", registryPort);
        registry.rebind(QuerySigner.NAME, stub);
    }
}
