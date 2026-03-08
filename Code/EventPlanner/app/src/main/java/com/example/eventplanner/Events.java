package com.example.eventplanner;

public class Events {
    //attributes of the events
    private String name;
    private String date;
    private String description;
    private String location;

    //constructor
    public Events(String name, String date, String description, String location) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.location = location;
    }

    //getters
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getLoction() {return location; }


    //setters
    public void setName(String name) { this.name = name; }
    public void setDate(String date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) {this.location = location;}
}