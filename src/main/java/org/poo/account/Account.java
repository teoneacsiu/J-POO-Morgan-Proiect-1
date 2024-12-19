package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.model.Card;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Account {
    private String iban;
    private double balance = 0.0;
    private String currency;
    private String type;
    private double minBalance = 0.0;  // New field for minimum balance
    private List<Card> cards = new ArrayList<>();

    /**
     * Constructs an Account instance.
     *
     * @param iban the IBAN of the account
     * @param currency the currency of the account
     * @param type the type of the account
     */
    public Account(final String iban, final String currency,
                   final String type) {
        this.iban = iban;
        this.currency = currency;
        this.type = type;
    }

    /**
     * Adds a card to the account.
     *
     * @param card the card to be added
     */
    public void addCard(final Card card) {
        cards.add(card);
    }

}
