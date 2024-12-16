package org.poo.model;

import lombok.Getter;
import lombok.Setter;
/**
 * Clasă derivată din Account, specifică pentru un cont de economii.
 * Adaugă suport pentru dobândă (interestRate).
 */
@Getter
@Setter
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(String iban, String currency, double interestRate) {
        super(iban, currency, "savings");
        this.interestRate = interestRate;
    }

}
