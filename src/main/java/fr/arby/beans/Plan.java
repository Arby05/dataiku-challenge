package fr.arby.beans;

import lombok.Data;

@Data
public class Plan {
    private int autonomy;
    private String departure;
    private String arrival;
    private String routesDB;
}
