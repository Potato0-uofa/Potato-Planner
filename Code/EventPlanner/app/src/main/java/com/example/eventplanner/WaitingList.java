package com.example.eventplanner;

import java.util.List;

public class WaitingList extends CustomLogs{
    private List<Entrant> entrants;

    public void addEntrant(Entrant entrant) {
        addItem(entrant);
    }

    public boolean hasEntrant(Entrant entrant) {
        return hasItem(entrant);
    }

    public void deleteEntrant(Entrant entrant) {
        deleteItem(entrant);
    }

    public int countEntrant() {
        return countItems();
    }
}
}
