package it.polimi.ds;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VectorClock implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> clock;

    public VectorClock(Map<String, Integer> clock) {
        this.clock = new HashMap<>();
        for (Map.Entry<String, Integer> entry : clock.entrySet()) {
            this.clock.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Integer> getClock() {
        return clock;
    }

    public void increment(String participant) {
        clock.put(participant, clock.get(participant) + 1);
    }

    public boolean isLessOrEqualThan(VectorClock otherClock) {
        Map<String, Integer> otherClockMap = otherClock.getClock();

        for (String participant : clock.keySet()) {
            if (clock.get(participant) > otherClockMap.get(participant)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return clock.toString();
    }
}
