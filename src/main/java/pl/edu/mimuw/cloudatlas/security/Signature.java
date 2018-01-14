package pl.edu.mimuw.cloudatlas.security;

import java.io.Serializable;

public class Signature implements Serializable {
    public Signature(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    private byte[] bytes;
}
