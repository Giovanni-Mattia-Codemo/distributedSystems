package it.polimi.ds;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VectorClock implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> clock;

    public VectorClock(Map<String, Integer> clock) {
        this.clock = new HashMap<>(clock);
    }

    public Map<String, Integer> getClock() {
        return clock;
    }

    public void increment(String participant) {
        clock.put(participant, clock.get(participant) + 1);
    }

    @Override
    public String toString() {
        return clock.toString();
    }
}
