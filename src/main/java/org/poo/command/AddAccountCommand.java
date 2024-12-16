package org.poo.command;

import org.poo.model.Account;
import org.poo.service.UserService;

/**
 * Comandă pentru adăugarea unui cont unui utilizator.
 */
public class AddAccountCommand implements Command {

    private final UserService userService;
    private final String email;
    private final String currency;
    private final String accountType;
    private final Double interestRate;

    public AddAccountCommand(UserService userService, String email, String currency, String accountType, Double interestRate) {
        this.userService = userService;
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.interestRate = interestRate;
    }

    @Override
    public void execute() {
        try {
            Account account = userService.addAccount(email, currency, accountType, interestRate, 0);
            System.out.println("Cont adăugat: IBAN=" + account.getIban() + ", Tip=" + account.getType() + ", Moneda=" + account.getCurrency());
        } catch (IllegalArgumentException e) {
            System.err.println("Eroare la crearea contului: " + e.getMessage());
        }
    }
}
