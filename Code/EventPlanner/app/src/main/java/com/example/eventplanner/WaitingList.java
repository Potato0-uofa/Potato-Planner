package com.example.eventplanner;

import java.util.ArrayList;

public class WaitingList {
    private ArrayList<Entrant> entrants;

    public WaitingList() {
        this.entrants = new ArrayList<>();
    }

    public void addEntrant(Entrant entrant) {
        if (entrant != null && !entrants.contains(entrant)) {
            entrants.add(entrant);
        }
    }

    public void removeEntrant(Entrant entrant) {
        entrants.remove(entrant);
    }

    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }

    public int getCount() {
        return entrants.size();
    }

    public boolean containsEntrant(Entrant entrant) {
        return entrants.contains(entrant);
    }
}