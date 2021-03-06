package pl.edu.mimuw.cloudatlas.modules.gossip;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

import java.io.Serializable;

public class GossipPackage implements Serializable {
    public GossipType type;
    public String nodeName;
    public AttributesMap[] info;
    public int zonesCnt;
    public long previousSndTimestamp;
    public long previousRcvTimestamp;
}
