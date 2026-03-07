package com.example.eventplanner;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomLogs {
    private List<Object> items;

    public CustomLogs() {
        this.items = new ArrayList<>();
    }

    public void addItem(Object item) {
        items.add(item);
    }

    public boolean hasItem(Object item) {
        return items.contains(item);
    }

    public void deleteItem(Object item) {
        items.remove(item);
    }

    public int countItems() {
        return items.size();
    }

}
