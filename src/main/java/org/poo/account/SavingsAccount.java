package org.poo.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavingsAccount extends Account {
    private double interestRate;

    /**
     * Constructs a SavingsAccount instance.
     *
     * @param iban the IBAN of the savings account
     * @param currency the currency of the savings account
     * @param interestRate the interest rate for the savings account
     */
    public SavingsAccount(final String iban, final String currency,
                          final double interestRate) {
        super(iban, currency, "savings");
        this.interestRate = interestRate;
    }

}
