package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SendMoneyTransaction extends Transaction {
    private final String senderIban;
    private final String receiverIban;
    private final double amount;
    private final String currency;
    private final String transactionType;

    /**
     * Constructs a SendMoneyTransaction instance.
     *
     * @param senderIban      the IBAN of the sender's account
     * @param receiverIban    the IBAN of the receiver's account
     * @param amount          the amount of money being sent
     * @param currency        the currency of the transaction
     * @param transactionType the type of the transaction (e.g., "sent", "received")
     * @param description     a description of the transaction
     * @param timestamp       the timestamp of the transaction
     * @param iban            the IBAN associated with the transaction
     */
    public SendMoneyTransaction(final String senderIban, final String receiverIban,
                                final double amount, final String currency,
                                final String transactionType, final String description,
                                final int timestamp, final String iban) {
        super(description, timestamp, iban);
        this.senderIban = senderIban;
        this.receiverIban = receiverIban;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        setType(TransactionType.SEND_MONEY);
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
        node.put("senderIBAN", senderIban);
        node.put("receiverIBAN", receiverIban);
        node.put("amount", amount + " " + currency);
        node.put("transferType", transactionType);
        return node;
    }
}
