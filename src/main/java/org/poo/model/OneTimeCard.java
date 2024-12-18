package org.poo.model;

import lombok.Getter;

@Getter
public class OneTimeCard extends Card {
    private boolean used; // Indicates if the card has been used

    public OneTimeCard(final String cardNumber, final String iban) {
        super(cardNumber, iban);
        this.used = false;
    }

    /**
     * javadoc
     */
    public boolean setUsed(final boolean b) {
        this.used = true;
        return this.used;
    }

    /**
     * javadoc
     */
    @Override
    public String getStatus() {
        return used ? "used" : "active";
    }

    /**
     * Use the card for a transaction. Once used, the card is marked as "used."
     * @throws IllegalStateException if the card has already been used.
     */
    public void useCard() {
        if (used) {
            throw new IllegalStateException("One-time card has already been used: "
                    + getCardNumber());
        }
        this.setUsed(true);
    }
}
