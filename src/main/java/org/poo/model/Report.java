package org.poo.model;

import lombok.Getter;
import lombok.Setter;
import org.poo.transactions.Transaction;

import java.util.List;

@Getter
@Setter
public class Report {
    private List<Transaction> transactions;
    private double balance;
    private String currency;

    public Report(List<Transaction> transactions, double balance, String currency) {
        this.transactions = transactions;
        this.balance = balance;
        this.currency = currency;
    }

}
