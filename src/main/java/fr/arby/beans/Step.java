package fr.arby.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Step {
    private String planetName;
    private int dayOfArrival;
    private boolean refuelOnPlanet;
    private int daysToWait;
    private int distanceFromPreviousJump;

    @Override
    public String toString() {
        return planetName;
    }
}
