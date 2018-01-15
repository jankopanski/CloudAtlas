package pl.edu.mimuw.cloudatlas.modules.gossip;

public class RoundRobinStrategy extends GossipStrategy {
    int lastLvl = 0;
    int lastLvlCount = 0;
    int switchCount;

    public RoundRobinStrategy(int lvls, int llb) {
        super(lvls, llb);
        switchCount = llb;
    }

    public int choseLevel() {
        if (lastLvlCount == switchCount) {
            lastLvlCount = 0;
            if (lastLvl == levels - 1) {
                lastLvl = 0;
                switchCount = lowLevelsBias;
            }
            else {
                lastLvl++;
                switchCount *= lowLevelsBias;
            }
        }
        lastLvlCount++;
        return lastLvl;
    }
}
