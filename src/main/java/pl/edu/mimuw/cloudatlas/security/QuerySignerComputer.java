package pl.edu.mimuw.cloudatlas.security;

import pl.edu.mimuw.cloudatlas.model.Attribute;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

public class QuerySignerComputer implements QuerySigner {
    private MessageDigest digestGenerator;
    private Cipher signCipher;
    private Map<String, String> queries = new HashMap<>();

    QuerySignerComputer(PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        digestGenerator = MessageDigest.getInstance(DIGEST_ALGORITHM);
        signCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
    }

    @Override
    public Signature sign(String query) throws InvalidQueryException, QueryConflictException {
        String queryName = new Attribute(query.split(":", 2)[0].trim()).getName();
        if (queryName.charAt(0) != '&') throw new IllegalArgumentException("Query name should start with &");
        if (queries.containsKey(queryName)) {
            throw new QueryConflictException(queries.get(queryName));
        }
        byte[] queryBytes = digestGenerator.digest(query.getBytes());
        try {
            byte[] encryptedBytes = signCipher.doFinal(queryBytes);
            queries.put(queryName, query);
            return new Signature(encryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new InvalidQueryException(e);
        }
    }
}
