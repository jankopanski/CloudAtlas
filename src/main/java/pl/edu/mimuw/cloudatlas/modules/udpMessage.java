package pl.edu.mimuw.cloudatlas.modules;

import java.io.Serializable;

public class udpMessage implements Serializable {
    int counter;
    int parts;
    int number;
    String nodeName;
    String data;
}
