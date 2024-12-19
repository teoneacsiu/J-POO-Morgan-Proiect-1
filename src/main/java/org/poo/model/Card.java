package org.poo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private String cardNumber;
    private String status; // "active" sau "inactive"
    private String associatedAccount;
    private boolean isOneTime;

    /**
     * Constructs a Card instance.
     *
     * @param cardNumber the card number
     * @param associatedAccount the account associated with the card
     */
    public Card(final String cardNumber, final String associatedAccount) {
        this.cardNumber = cardNumber;
        this.associatedAccount = associatedAccount;
        this.status = "active";
    }
}
