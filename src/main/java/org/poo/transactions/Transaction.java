package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Transaction {
    private int timestamp;
    private String description;
    private boolean isInterest;

    public Transaction(final String description, final int timestamp) {
        this.description = description;
        this.timestamp = timestamp;
        this.isInterest = false;
    }

    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", description);
        return node;
    }
}
