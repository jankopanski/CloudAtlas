package pl.edu.mimuw.cloudatlas.modules.gossip;

import java.util.Random;

public class RandomStrategy extends GossipStrategy {
    private Random rng = new Random();
    int randomHelper;

    public RandomStrategy(int lvls, int llb) {
        super(lvls, llb);
        randomHelper = 0;
        int j = llb;
        for (int i = 0; i < lvls; i++, j *= llb)
            randomHelper += j;
    }

    public int choseLevel() {
         int rand = rng.nextInt(randomHelper);
         int i = lowLevelsBias;
         int result = -1;
         do {
             rand -= i;
             i *= lowLevelsBias;
             result++;
         } while (rand >= 0);
         return result;
    }
}
