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

    /**
     * Constructs a SplitPaymentTransaction instance.
     *
     * @param timestamp   the timestamp of the transaction
     * @param description a description of the transaction
     * @param currency    the currency of the payment
     * @param sum         the total amount to be split
     * @param accountList the list of accounts involved in the split payment
     */
    public SplitPaymentTransaction(final int timestamp, final String description,
                                   final String currency, final double sum,
                                   final List<String> accountList) {
        super(description, timestamp);
        this.currency = currency;
        this.sum = sum;
        this.accountList = accountList;
        setType(TransactionType.SPLIT_PAYMENT);
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
