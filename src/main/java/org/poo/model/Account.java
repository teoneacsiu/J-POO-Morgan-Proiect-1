package org.poo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Account {
    private String iban;
    private double balance = 0.0;
    private String currency;
    private String type;
    private double minBalance = 0.0;  // New field for minimum balance
    private List<Card> cards = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    public Account(String iban, String currency, String type) {
        this.iban = iban;
        this.currency = currency;
        this.type = type;
    }

    // Add a transaction to the account
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    // Add a card to the account
    public void addCard(Card card) {
        cards.add(card);
    }

}
