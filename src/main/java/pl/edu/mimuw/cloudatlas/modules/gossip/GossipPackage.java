package pl.edu.mimuw.cloudatlas.modules.gossip;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

import java.io.Serializable;

public class GossipPackage implements Serializable {
    public GossipType type;
    public String nodeName;
    //public ValueTime[] freshness;
    public AttributesMap[] info;
}
