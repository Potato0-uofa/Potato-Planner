package com.example.eventplanner;

import java.util.ArrayList;
import java.util.List;

public class WaitingList extends CustomLogs {
    private List<Entrant> entrants;

    public WaitingList() {
        this.entrants = new ArrayList<>();
    }

    public void addEntrant(Entrant entrant) {
        if (entrant != null && !entrants.contains(entrant)) {
            entrants.add(entrant);
        }
    }

    public boolean hasEntrant(Entrant entrant) {
        return entrants.contains(entrant);
    }

    public boolean containsEntrant(Entrant entrant) {
        return entrants.contains(entrant);
    }

    public void deleteEntrant(Entrant entrant) {
        entrants.remove(entrant);
    }

    public void removeEntrant(Entrant entrant) {
        entrants.remove(entrant);
    }

    public List<Entrant> getEntrants() {
        return entrants;
    }

    public int getCount() {
        return entrants.size();
    }

    public int countEntrants() {
        return entrants.size();
    }
}