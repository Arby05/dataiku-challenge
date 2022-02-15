package fr.arby.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Step {
    private String planetName;
    private Integer distanceFromPreviousJump;

    @Override
    public String toString() {
        return planetName;
    }
}