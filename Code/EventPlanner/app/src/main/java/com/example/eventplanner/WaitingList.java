package com.example.eventplanner;

import java.util.ArrayList;
import java.util.List;

public class WaitingList extends CustomLogs{
    private List<Entrant> waitingList;
    public WaitingList(){
        this.entrants = new ArrayList<>();
    }

    public void addEntrant(Entrant entrant) {
        /**
         * Adds an entrant to the waiting list.
         * @param entrant Entrant to be added
         */
        waitingList.add(entrant);
    }

    public boolean hasEntrant(Entrant entrant) {
        /**
         * Checks to see if the entrant is in the list or not.
         * @param entrant Entrant that needs to be checked
         * @return True if entrant is in the list and false if not
         */
        return waitingList.contains(entrant);
    }

    public void deleteEntrant(Entrant entrant) {
        /**
         * Remove entrant from waiting list.
         * @param entrant Entrant to be removed
         */
        waitingList.remove(entrant);
    }

    public int countEntrants() {
        /**
         * Checks to see how many entrants are in the waiting list.
         * @return int value that represents how many entrants there are
         */
        return countItems();
    }
}

