package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PayOnlineTransaction extends Transaction {
    private final double amount;
    private final String commerciant;

    public PayOnlineTransaction(final double amount, final String commerciant,
                                final String description, final int timestamp) {
        super(description, timestamp);
        this.amount = amount;
        this.commerciant = commerciant;
    }

    @Override
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", getTimestamp());
        node.put("description", getDescription());
        node.put("amount", amount);
        node.put("commerciant", commerciant);
        return node;
    }
}
