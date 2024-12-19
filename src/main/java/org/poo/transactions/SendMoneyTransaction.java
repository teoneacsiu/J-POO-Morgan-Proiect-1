package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SendMoneyTransaction extends Transaction {
    private final String senderIban;
    private final String receiverIban;
    private final double amount;
    private final String currency;
    private final String transactionType;

    public SendMoneyTransaction(final String senderIban, final String receiverIban,
                                final double amount, final String currency,
                                final String transactionType, final String description,
                                final int timestamp) {
        super(description, timestamp);
        this.senderIban = senderIban;
        this.receiverIban = receiverIban;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
    }

    @Override
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", getTimestamp());
        node.put("description", getDescription());
        node.put("senderIBAN", senderIban);
        node.put("receiverIBAN", receiverIban);
        node.put("amount", amount + " " + currency);
        node.put("transferType", transactionType);
        return node;
    }
}
