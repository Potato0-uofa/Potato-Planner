package com.example.eventplanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the waiting list for an event, maintaining a local list of object Entrant
 *
 */
public class WaitingList extends CustomLogs {

    /** Local list of entrants currently on the waiting list. */
    private List<Entrant> entrants;

    /**
     * The waitlist count synced from Firestore.
     * When greater than zero, this value takes precedence over the local list size.
     */
    private int cloudCount = 0;


    /**
     * Constructs an empty waiting list.
     */
    public WaitingList() {
        this.entrants = new ArrayList<>();
    }

    /**
     * Adds an entrant to the waiting list if they are not already present.
     * @param entrant the Entrant to be added
     */
    public void addEntrant(Entrant entrant) {
        if (entrant != null && !entrants.contains(entrant)) {
            entrants.add(entrant);
        }
    }

    /**
     * Returns whether the given entrant is on the waiting list.
     * @param entrant Entrant to be checked
     * @return true if entrant is on the list and false if not
     */
    public boolean hasEntrant(Entrant entrant) {
        return entrants.contains(entrant);
    }

    /**
     * Removes a specified entrant from the waiting list
     * @param entrant Entrant to be removed
     */
    public void deleteEntrant(Entrant entrant) {
        entrants.remove(entrant);
    }

    /**
     * Returns a copy of waiting list
     * @return a new array list containing the current entrants
     */
    public ArrayList<Entrant> getEntrants() {
        return new ArrayList<>(entrants);
    }

    /**
     * Sets the cloud-synced waitlist count from Firestore.
     * When this value is greater than zero, it takes precedence over the local list size
     * @param count the count of the list from firestore
     */
    public void setCloudCount(int count) {
        this.cloudCount = count;
    }

    /**
     * Returns the current waitlist count, preferring the cloud count when available.
     * @return cloudcount if size is greater than 0 or size of local list if not
     */
    public int getCount() {
        return (cloudCount > 0) ? cloudCount : entrants.size();
    }

}