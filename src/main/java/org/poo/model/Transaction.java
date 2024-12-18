package org.poo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

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
    private List<String> accounts;

    public Transaction(final String description, final int timestamp) {
        this.description = description;
        this.timestamp = timestamp;
    }

    // Constructor pentru tranzacții simple
    public Transaction(final int timestamp, final String description,
                       final String commandType) {
        this.timestamp = timestamp;
        this.description = description;
        this.commandType = commandType;
    }

    // Constructor pentru sendMoney
    public Transaction(final int timestamp, final String description,
                       final String senderIBAN, final String receiverIBAN,
                       final double amount, final String currency,
                       final String transferType, final String commandType) {
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
    public Transaction(final int timestamp, final String description,
                       final String cardNumber, final String cardHolder,
                       final String commerciant, final double amount,
                       final String currency, final String commandType) {
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
    public Transaction(final int timestamp, final String cardNumber,
                       final String cardHolder, final String senderIBAN,
                       final String commandType) {
        this.timestamp = timestamp;
        this.description = "The card has been destroyed";
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.senderIBAN = senderIBAN;
        this.commandType = commandType;
    }

    public Transaction(final String description, final int timestamp,
                       final String cardNumber, final String cardHolder,
                       final String senderIBAN) {
        this.description = description;
        this.timestamp = timestamp;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.senderIBAN = senderIBAN;
    }

    public Transaction(final int timestamp, final String description,
                       final double amount, final String commerciant) {
        this.timestamp = timestamp;
        this.description = description;
        this.amount = amount;
        this.commerciant = commerciant;
    }

    public Transaction(final String commandType, final int timestamp,
                       final String cardNumber, final String cardHolder) {
        this.commandType = commandType;
        this.timestamp = timestamp;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
    }

    public Transaction(final int timestamp, final String description,
                       final String currency, final double splitSum,
                       final List<String> accounts) {
        this.timestamp = timestamp;
        this.description = description;
        this.currency = currency;
        this.amount = splitSum;
        this.accounts = accounts;
    }
}
