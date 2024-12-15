package org.poo.model;

public class Transaction {
    private int timestamp;
    private String description;

    public Transaction(int timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
