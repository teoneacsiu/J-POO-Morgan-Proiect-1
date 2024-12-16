package org.poo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Transaction {
    private int timestamp;
    private String description;
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private String currency;
    private String transferType; // e.g., "sent" or "received"

    public Transaction(int timestamp, String s) {
        this.timestamp = timestamp;
        this.description = s;
    }
}
