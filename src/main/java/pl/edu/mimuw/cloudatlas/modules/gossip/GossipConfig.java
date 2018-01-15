package pl.edu.mimuw.cloudatlas.modules.gossip;

import lombok.Data;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.time.Duration;

@Data
public class GossipConfig {
    private Duration gossipTimeout;
    private Duration updateTimeout;
    private String strategy;
    private int levels;
    private int switches;
    private int port;
    private int maxContacts;
    private PathName path;
}
