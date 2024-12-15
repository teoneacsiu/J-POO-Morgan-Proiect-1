package org.poo.model;

import lombok.Getter;

@Getter
public class OneTimeCard extends Card {
    private boolean used; // Indicates if the card has been used

    public OneTimeCard(String cardNumber, String iban) {
        super(cardNumber, iban);
        this.used = false;
    }

    public boolean setUsed(boolean b) {
        this.used = true;
        return this.used;
    }

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
            throw new IllegalStateException("One-time card has already been used: " + getCardNumber());
        }
        this.setUsed(true);
    }
}
