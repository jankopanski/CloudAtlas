package pl.edu.mimuw.cloudatlas.security;

import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class QuerySignerServer {
    private static String privateKeyFile;
    private static String registryHost;
    private static int registryPort;


    public static void main(String args[]) throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        if (args.length == 3) {
            privateKeyFile = args[0];
            registryHost = args[1];
            registryPort = Integer.parseInt(args[2]);
        }
        else {
            System.err.println("Usage: ./signer.sh <registry_port>");
        }

        BufferedReader reader = new BufferedReader(new FileReader(privateKeyFile));
        String privateKeyString = reader.readLine();
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] privateKeyBytes = decoder.decode(privateKeyString);

        KeyFactory keyFactory = KeyFactory.getInstance(QuerySigner.ENCRYPTION_ALGORITHM);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        QuerySignerComputer object = new QuerySignerComputer(privateKey);
        QuerySigner stub = (QuerySigner) UnicastRemoteObject.exportObject(object, 0);
        Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
        registry.rebind(QuerySigner.NAME, stub);
    }
}
