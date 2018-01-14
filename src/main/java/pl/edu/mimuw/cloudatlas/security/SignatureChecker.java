package pl.edu.mimuw.cloudatlas.security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

public class SignatureChecker {
    private MessageDigest digestGenerator;
    private Cipher verifyCipher;

    public SignatureChecker(PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        digestGenerator = MessageDigest.getInstance(QuerySigner.DIGEST_ALGORITHM);
        verifyCipher = Cipher.getInstance(QuerySigner.ENCRYPTION_ALGORITHM);
        verifyCipher.init(Cipher.DECRYPT_MODE, publicKey);
    }

    public boolean check(String query, Signature signature) throws InvalidQueryException {
        byte[] queryBytes = digestGenerator.digest(query.getBytes());
        try {
            byte[] decryptedBytes = verifyCipher.doFinal(signature.getBytes());
            return Arrays.equals(queryBytes, decryptedBytes);
        } catch (Exception e) {
            throw new InvalidQueryException(e);
        }
    }
}
