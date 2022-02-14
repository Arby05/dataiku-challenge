package fr.arby.beans;

import lombok.Data;

import java.util.List;

@Data
public class Empire {
    private int countdown;
    private List<BountyHunter> bounty_hunters;
}
