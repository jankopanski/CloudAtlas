package pl.edu.mimuw.cloudatlas.security;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.util.Base64;

public class KeyGenerator {
    private final static String PRIVATE_KEY_FILE = "query_signer.private";
    private final static String PUBLIC_KEY_FILE = "query_signer.public";

    public static void main(String args[]) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(QuerySigner.ENCRYPTION_ALGORITHM);
        keyGenerator.initialize(QuerySigner.NUM_KEY_BITS);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Base64.Encoder encoder = Base64.getEncoder();
        BufferedWriter writer;

        byte[] privateKeyBytes = privateKey.getEncoded();
        String privateKeyString = encoder.encodeToString(privateKeyBytes);
        writer = new BufferedWriter(new FileWriter(PRIVATE_KEY_FILE));
        writer.write(privateKeyString);
        writer.close();

        byte[] publicKeyBytes = publicKey.getEncoded();
        String publicKeyString = encoder.encodeToString(publicKeyBytes);
        writer = new BufferedWriter(new FileWriter(PUBLIC_KEY_FILE));
        writer.write(publicKeyString);
        writer.close();
//        System.out.println(Arrays.toString(privateKey.getEncoded()));
//        System.out.println(privateKeyString);
//        System.out.println(publicKeyString);
//        Base64.Decoder decoder = Base64.getDecoder();
//        System.out.println(Arrays.toString(decoder.decode(privateString)));
    }
}
