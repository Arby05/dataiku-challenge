package fr.arby.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Step {
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
}
