package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateCardTransaction extends Transaction{
    private final String cardNum;
    private final String cardHolder;
    private final String account;

    public CreateCardTransaction(final String cardNum, final String cardHolder,
                                 final String account, final int timestamp,
                                 final String description) {
        super(description, timestamp);
        this.cardNum = cardNum;
        this.cardHolder = cardHolder;
        this.account = account;
    }

    @Override
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", getTimestamp());
        node.put("description", getDescription());
        node.put("card", cardNum);
        node.put("cardHolder", cardHolder);
        node.put("account", account);
        return node;
    }
}
