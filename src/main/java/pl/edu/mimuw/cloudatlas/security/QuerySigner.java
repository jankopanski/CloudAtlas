package pl.edu.mimuw.cloudatlas.security;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.rmi.Remote;

public interface QuerySigner extends Remote {
    public final static String NAME = "QuerySigner";
    public final static String DIGEST_ALGORITHM = "SHA-256";
    public final static String ENCRYPTION_ALGORITHM = "RSA";
    public final static int NUM_KEY_BITS = 1024;

    // Given a query, returns signature encrypted with Query Signer private key
    public Signature sign(String query) throws BadPaddingException, IllegalBlockSizeException, QueryConflictException;
}
