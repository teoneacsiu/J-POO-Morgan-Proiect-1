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

    /**
     * Constructs a Report instance.
     *
     * @param transactions the list of transactions included in the report
     * @param balance the balance of the account at the time of the report
     * @param currency the currency of the account
     */
    public Report(final List<Transaction> transactions,
                  final double balance, final String currency) {
        this.transactions = transactions;
        this.balance = balance;
        this.currency = currency;
    }
}
