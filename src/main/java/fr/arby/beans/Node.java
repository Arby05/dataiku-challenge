package fr.arby.beans;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Node {
    private String planetName;
    @Builder.Default
    private List<Integer> daysWithBountyHunters = new ArrayList<>();
    @Builder.Default
    private Map<String, Integer> adjacentNodes = new HashMap<>();

    public void addDestination(String destination, int distance) {
        adjacentNodes.put(destination, distance);
    }

    public void addBountyHunter(int day) {
        daysWithBountyHunters.add(day);
    }

    @Override
    public String toString() {
        return planetName;
    }
}
