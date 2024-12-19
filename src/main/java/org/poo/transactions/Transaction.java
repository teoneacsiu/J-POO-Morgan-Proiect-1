package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a generic financial transaction.
 */
@Getter
@Setter
public class Transaction {
    private int timestamp;
    private String description;
    private TransactionType type;
    private String iban;

    /**
     * Constructs a Transaction with a description, timestamp, and associated IBAN.
     *
     * @param description a description of the transaction
     * @param timestamp   the timestamp of the transaction
     * @param iban        the IBAN associated with the transaction
     */
    public Transaction(final String description, final int timestamp, final String iban) {
        this.description = description;
        this.timestamp = timestamp;
        this.iban = iban;
    }

    /**
     * Constructs a Transaction with a description and timestamp.
     *
     * @param description a description of the transaction
     * @param timestamp   the timestamp of the transaction
     */
    public Transaction(final String description, final int timestamp) {
        this.description = description;
        this.timestamp = timestamp;
    }

    /**
     * Converts the transaction details into a JSON representation.
     *
     * @return an ObjectNode containing the transaction details
     */
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", description);
        return node;
    }
}
