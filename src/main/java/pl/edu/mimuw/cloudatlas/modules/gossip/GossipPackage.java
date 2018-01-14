package pl.edu.mimuw.cloudatlas.modules.gossip;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

import java.io.Serializable;

public class GossipPackage implements Serializable {
    GossipType type;
    ValueTime[] freshness;
    AttributesMap[] info;
}
