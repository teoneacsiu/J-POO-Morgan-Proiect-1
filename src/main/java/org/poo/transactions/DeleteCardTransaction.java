package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DeleteCardTransaction extends Transaction {
    private static final String description = "The card has been destroyed";
    private final String cardHolder;
    private final String cardNumber;

    public DeleteCardTransaction(final String cardHolder, final String cardNumber,
                                 final int timestamp) {
        super(description, timestamp);
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
    }

    @Override
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", getTimestamp());
        node.put("description", getDescription());
        node.put("card", cardNumber);
        node.put("cardHolder", cardHolder);
        return node;
    }
}
