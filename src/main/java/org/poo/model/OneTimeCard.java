package org.poo.model;

import lombok.Getter;

@Getter
public class OneTimeCard extends Card {
    private boolean used; // Indicates if the card has been used

    public OneTimeCard(final String cardNumber, final String iban) {
        super(cardNumber, iban);
        this.used = false;
        setOneTime(true);
    }

    /**
     * Marks the card as used and returns the updated status.
     *
     * @param b a boolean value indicating the card's new usage state (not used).
     * @return true if the card is marked as used, false otherwise.
     */
    public boolean setUsed(final boolean b) {
        this.used = true;
        return this.used;
    }

    /**
     * Returns the status of the card.
     *
     * @return "used" if the card has been used, otherwise "active".
     */
    @Override
    public String getStatus() {
        return used ? "used" : "active";
    }
}
