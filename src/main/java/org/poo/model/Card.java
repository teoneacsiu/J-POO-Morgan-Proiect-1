package org.poo.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Clasă de bază pentru un card asociat unui cont bancar.
 */
@Getter
@Setter
public class Card {
    private String cardNumber;
    private String status; // "active" sau "inactive"
    private String associatedAccount;

    public Card(final String cardNumber, final String associatedAccount) {
        this.cardNumber = cardNumber;
        this.associatedAccount = associatedAccount;
        this.status = "active";
    }
}
