package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;

import java.util.LinkedList;

public class AverageCounter {
    private int size;
    private LinkedList<AttributesMap> queue;

    public AverageCounter(int collectionInterval, int averagingPeriod) {
        size = (int) Math.ceil(averagingPeriod / collectionInterval);
        queue = new LinkedList<>();
    }

    public void add(AttributesMap attributesMap) {
        if (queue.size() >= size) {
            queue.remove();
        }
        queue.add(attributesMap);
    }

    AttributesMap get() {
        return queue.peek();
    }
}
