package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayOnlineTransaction extends Transaction {
    private final double amount;
    private final String commerciant;

    public PayOnlineTransaction(final double amount, final String commerciant,
                                final String description, final int timestamp,
                                final String iban) {
        super(description, timestamp, iban);
        this.amount = amount;
        this.commerciant = commerciant;
        setType(TransactionType.PAY_ONLINE);
    }

    /**
     * Converts the transaction details into a JSON representation.
     *
     * @return an ObjectNode containing the transaction details
     */
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
