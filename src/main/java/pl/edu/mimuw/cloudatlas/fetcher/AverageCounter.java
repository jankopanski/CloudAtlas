package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.model.*;

import java.util.LinkedList;
import java.util.Map;

public class AverageCounter {
    private String method;
    private int size;
    private LinkedList<AttributesMap> queue;

    public AverageCounter(String method, int collectionInterval, int averagingPeriod) {
        this.method = method;
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
        switch (method) {
            case "average":
                return average();
            default:
                return queue.peekLast();
        }
    }

    private AttributesMap average() {
        AttributesMap map = queue.peekLast().clone();
        for (Map.Entry<Attribute, Value> entry : map) {
            Value val = entry.getValue();
            if (entry.getValue().getType() == TypePrimitive.DOUBLE) {
                val = new ValueDouble(0.0);
                for (AttributesMap info : queue) {
                    val = val.addValue(info.get(entry.getKey()));
                }
                val = val.divide(new ValueDouble((double) size));
            }
            else if (entry.getValue().getType() == TypePrimitive.INTEGER) {
                val = new ValueInt(0L);
                for (AttributesMap info : queue) {
                    val = val.addValue(info.get(entry.getKey()));
                }
                val = val.divide(new ValueInt((long) size));
                val = val.convertTo(TypePrimitive.INTEGER);
            }
            map.addOrChange(entry.getKey(), val);
        }
        return map;
    }
}
