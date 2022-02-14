package fr.arby.beans;

import lombok.Data;

import java.util.List;

@Data
public class Node {
    private String planetName;
    private List<Integer> daysWithBountyHunters;
}
