package org.poo.transactions;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class InterestTransaction extends Transaction {
    private final String iban;
    private final double interest;
    private final String currency;

    public InterestTransaction(final String iban, final double interest,
                               final String currency, final String description,
                               final int timestamp) {
        super(description, timestamp);
        this.iban = iban;
        this.interest = interest;
        this.currency = currency;
        setInterest(true);
    }

    @Override
    public ObjectNode toJson() {
        return null;
    }
}
