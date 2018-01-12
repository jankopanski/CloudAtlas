package pl.edu.mimuw.cloudatlas.modules;

import lombok.Data;

@Data
public class Message {
    public Module source;
    public Module destination;
    public msgType type;
}
