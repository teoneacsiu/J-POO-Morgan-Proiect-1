package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DeleteCardTransaction extends Transaction {
    private static final String DESCRIPTION = "The card has been destroyed";
    private final String cardHolder;
    private final String cardNumber;

    /**
     * Constructs a DeleteCardTransaction instance.
     *
     * @param cardHolder  the holder of the card being deleted
     * @param cardNumber  the number of the card being deleted
     * @param timestamp   the timestamp of the transaction
     * @param iban        the IBAN of the account associated with the card
     */
    public DeleteCardTransaction(final String cardHolder, final String cardNumber,
                                 final int timestamp, final String iban) {
        super(DESCRIPTION, timestamp, iban);
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        setType(TransactionType.DELETE_CARD);
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
        node.put("card", cardNumber);
        node.put("cardHolder", cardHolder);
        node.put("account", getIban());
        return node;
    }
}
