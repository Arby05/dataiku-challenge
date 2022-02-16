package fr.arby.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Step implements Cloneable {
    private String planetName;
    private int dayOfArrival;
    private int daysToWait;
    private int distanceFromPreviousJump;
    private int riskedEncounter;
    private boolean isRefuel;

    @Override
    public String toString() {
        return planetName;
    }

    public void removeEncounter() {
        if (isRefuel) {
            riskedEncounter = riskedEncounter - 2;
        }
        else {
            riskedEncounter--;
        }
    }

    @Override
    public Step clone() {
        return Step.builder()
                .planetName(this.planetName)
                .dayOfArrival(this.dayOfArrival)
                .daysToWait(this.daysToWait)
                .distanceFromPreviousJump(this.distanceFromPreviousJump)
                .riskedEncounter(this.riskedEncounter)
                .isRefuel(this.isRefuel)
                .build();
    }
}
