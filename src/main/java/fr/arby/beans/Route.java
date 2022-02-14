package fr.arby.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Route {
    private String origin;
    private String destination;
    private int travelTime;
}
