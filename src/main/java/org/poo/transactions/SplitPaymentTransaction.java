package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SplitPaymentTransaction extends Transaction {
    private final String currency;
    private final double sum;
    private final List<String> accountList;
    private String error;

    public SplitPaymentTransaction(final int timestamp, final String description,
                                   final String currency, final double sum,
                                   final List<String> accountList) {
        super(description, timestamp);
        this.currency = currency;
        this.sum = sum;
        this.accountList = accountList;
    }

    @Override
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("timestamp", getTimestamp());
        node.put("description", getDescription());
        node.put("currency", currency);
        node.put("amount", sum);
        node.putArray("involvedAccounts").addAll(
                mapper.convertValue(accountList, ArrayNode.class));
        if (error != null) {
            node.put("error", error);
        }
        return node;
    }
}
