package pl.edu.mimuw.cloudatlas.security;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QuerySigner extends Remote {
    public final static String NAME = "QuerySigner";
    public final static String DIGEST_ALGORITHM = "SHA-256";
    public final static String ENCRYPTION_ALGORITHM = "RSA";
    public final static int NUM_KEY_BITS = 1024;

    // Given a query, returns signature encrypted with Query Signer private key
    public Signature sign(String query) throws RemoteException, QueryConflictException, InvalidQueryException;
}
