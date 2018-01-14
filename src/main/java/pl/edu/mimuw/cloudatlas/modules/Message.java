package pl.edu.mimuw.cloudatlas.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class Message {
    public Module source;
    public Module destination;
    public msgType type;

}
