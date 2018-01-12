package pl.edu.mimuw.cloudatlas.modules;

import java.net.InetAddress;

public class CommunicationMessage extends Message {
    InetAddress destIP;
    String data;
}
