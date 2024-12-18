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
    private String senderIBAN;    // IBAN al expeditorului
    private String receiverIBAN;  // IBAN al destinatarului
    private double amount;        // Suma tranzacției
    private String currency;      // Moneda tranzacției
    private String transferType;  // Tipul tranzacției: "sent", "received", "payment"
    private String cardNumber;    // Opțional: Numărul cardului pentru tranzacțiile cu carduri
    private String cardHolder;    // Opțional: Deținătorul cardului pentru tranzacțiile cu carduri
    private String commerciant;   // Opțional: Numele comerciantului (pentru tranzacțiile online)
    private String commandType;   // Nou: Tipul comenzii care a generat tranzacția

    // Constructor pentru tranzacții simple
    public Transaction(int timestamp, String description, String commandType) {
        this.timestamp = timestamp;
        this.description = description;
        this.commandType = commandType;
    }

    // Constructor pentru sendMoney
    public Transaction(int timestamp, String description, String senderIBAN, String receiverIBAN, double amount, String currency, String transferType, String commandType) {
        this.timestamp = timestamp;
        this.description = description;
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.currency = currency;
        this.transferType = transferType;
        this.commandType = commandType;
    }

    // Constructor pentru card payment
    public Transaction(int timestamp, String description, String cardNumber, String cardHolder, String commerciant, double amount, String currency, String commandType) {
        this.timestamp = timestamp;
        this.description = description;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.commerciant = commerciant;
        this.amount = amount;
        this.currency = currency;
        this.transferType = "payment";
        this.commandType = commandType;
    }

    // Constructor pentru deleteCard
    public Transaction(int timestamp, String cardNumber, String cardHolder, String senderIBAN, String commandType) {
        this.timestamp = timestamp;
        this.description = "The card has been destroyed";
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.senderIBAN = senderIBAN;
        this.commandType = commandType;
    }

    // Metodă pentru descriere formatată
    public String getFormattedDescription() {
        if ("deleteCard".equals(commandType)) {
            return "The card has been destroyed";
        } else if ("sendMoney".equals(commandType)) {
            return "Transfer from " + senderIBAN + " to " + receiverIBAN;
        } else if ("payOnline".equals(commandType)) {
            return "Card payment to " + commerciant;
        } else if ("addAccount".equals(commandType)) {
            return "New account created";
        }
        return description;
    }
}
