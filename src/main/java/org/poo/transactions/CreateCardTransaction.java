package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateCardTransaction extends Transaction {
    private final String cardNum;
    private final String cardHolder;
    private final String account;

    /**
     * Constructs a CreateCardTransaction instance.
     *
     * @param cardNum      the number of the card being created
     * @param cardHolder   the holder of the card
     * @param account      the account associated with the card
     * @param timestamp    the timestamp of the transaction
     * @param description  a description of the transaction
     * @param iban         the IBAN of the account associated with the transaction
     */
    public CreateCardTransaction(final String cardNum, final String cardHolder,
                                 final String account, final int timestamp,
                                 final String description, final String iban) {
        super(description, timestamp, iban);
        this.cardNum = cardNum;
        this.cardHolder = cardHolder;
        this.account = account;
        setType(TransactionType.CREATE_CARD);
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
        node.put("card", cardNum);
        node.put("cardHolder", cardHolder);
        node.put("account", account);
        return node;
    }
}
