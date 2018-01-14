package pl.edu.mimuw.cloudatlas.modules.gossip;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class GossipStrategy {
    int levels;
    int lowLevelsBias;

    public abstract int choseLevel();
}
