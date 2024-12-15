package org.poo.model;

/**
 * Clasă derivată din Account, specifică pentru un cont de economii.
 * Adaugă suport pentru dobândă (interestRate).
 */
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(String iban, String currency, double interestRate) {
        super(iban, currency, "savings");
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
}
