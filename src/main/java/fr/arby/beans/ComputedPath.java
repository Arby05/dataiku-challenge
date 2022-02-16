package fr.arby.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComputedPath {
    private List<Step> steps;
    private Integer totalDistance = 0;
    private Integer refuelNumber = 0;
    private Integer bountyHunterEncounter = 0;
    private Double probability;

    @Override
    public String toString() {
        int totalDays = totalDistance + refuelNumber;
        return steps + " - distance : " + totalDistance + " - refuel : " + refuelNumber + " - BH : " + bountyHunterEncounter + " - total day passed : " + totalDays;
    }

    public void refuel() {
        refuelNumber++;
    }

    public void bountyHunterSpotted(int count) {
        bountyHunterEncounter = bountyHunterEncounter + count;
    }

    public void revertRefuel() {
        refuelNumber--;
    }

    public void revertBountyHunterSpotted(int count) {
        bountyHunterEncounter = bountyHunterEncounter - count;
    }

    public void addStep (Step step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }

    public ComputedPath (ComputedPath toCopy) {
        this.refuelNumber = toCopy.refuelNumber;
        this.totalDistance = toCopy.totalDistance;
        this.steps = new ArrayList<>(toCopy.steps);
        this.bountyHunterEncounter = toCopy.bountyHunterEncounter;
    }

    public void computeProbability() {
        probability = 1d;
        for (int i = 0; i < bountyHunterEncounter; i++) {
            probability = probability - (Math.pow(9, i) / Math.pow(10, i+1));
        }
    }
}
