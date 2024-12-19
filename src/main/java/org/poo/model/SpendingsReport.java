package org.poo.model;

import lombok.Getter;
import lombok.Setter;
import org.poo.transactions.PayOnlineTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SpendingsReport {
    private final List<PayOnlineTransaction> transactions;
    private double balance;
    private String currency;
    private Map<String, Double> commerciants = new HashMap<>();

    /**
     * Constructs a SpendingsReport instance.
     *
     * @param transactions the list of online payment transactions included in the report
     * @param balance the balance of the account at the time of the report
     * @param currency the currency of the account
     */
    public SpendingsReport(final List<PayOnlineTransaction> transactions,
                           final double balance, final String currency) {
        this.transactions = transactions;
        this.balance = balance;
        this.currency = currency;
    }

    /**
     * Populates the list of commerciants with the total spending for each commerciant.
     */
    public void setCommerciants() {
        for (final PayOnlineTransaction transaction : transactions) {
            double amount = transaction.getAmount();
            if (transaction.getCommerciant() != null) {
                commerciants.put(transaction.getCommerciant(),
                        commerciants.getOrDefault(transaction.getCommerciant(), 0.0) + amount);
            }
        }
    }
}
