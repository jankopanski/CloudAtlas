package pl.edu.mimuw.cloudatlas.modules.gossip;

import java.io.Serializable;

public enum GossipType implements Serializable {
    //FRESHNESS, ZMIS, FRESHENESS_AND_ZMIS
    INITIAL, RETURN, FINAL
}
