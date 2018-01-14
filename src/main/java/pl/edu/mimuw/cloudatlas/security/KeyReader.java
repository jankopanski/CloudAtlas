package pl.edu.mimuw.cloudatlas.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyReader {
    public static PrivateKey readPrivateKey(String privateKeyFile) throws KeyReaderException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(QuerySigner.ENCRYPTION_ALGORITHM);
            byte[] privateKeyBytes = readKey(privateKeyFile);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        }
        catch (Exception e) {
            throw new KeyReaderException(e);
        }
    }

    public static PublicKey readPublicKey(String publicKeyFile) throws KeyReaderException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(QuerySigner.ENCRYPTION_ALGORITHM);
            byte[] publicKeyBytes = readKey(publicKeyFile);
            EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            return keyFactory.generatePublic(privateKeySpec);
        }
        catch (Exception e) {
            throw new KeyReaderException(e);
        }
    }

    private static byte[] readKey(String keyFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(keyFile));
        String keyString = reader.readLine();
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(keyString);
    }
}
