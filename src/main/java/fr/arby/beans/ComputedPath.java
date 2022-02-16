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

    @Override
    public String toString() {
        return steps + " - distance : " + getTotalDistance() + " - refuel : " + getTotalRefuel() + " - BH : " + getTotalBountyHunterEncounter() + " - total day passed : " + getTotalDays();
    }


    public void addStep (Step step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }

    /**
     * Constructeur de copy
     * @param toCopy ComputedPath à copier
     */
    public ComputedPath (ComputedPath toCopy) {
        this.steps = new ArrayList<>(toCopy.steps);
    }

    public double computeProbability() {
        double probability = 1d;
        for (int i = 0; i < getTotalBountyHunterEncounter(); i++) {
            probability = probability - (Math.pow(9, i) / Math.pow(10, i+1));
        }
        return probability;
    }

    public int getTotalBountyHunterEncounter() {
        return steps.stream().mapToInt(Step::getRiskedEncounter).sum();
    }

    public int getTotalDistance() {
        return steps.stream().mapToInt(Step::getDistanceFromPreviousJump).sum();
    }

    public int getTotalRefuel() {
        return (int) steps.stream().filter(Step::isRefuel).count();
    }

    public int getTotalDays() {
        return getTotalDistance() + getTotalRefuel() + steps.stream().mapToInt(Step::getDaysToWait).sum();
    }
}
