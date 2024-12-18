package org.poo.command;

import org.poo.service.UserService;

/**
 * Comandă pentru adăugarea de fonduri într-un cont.
 */
public class AddFundsCommand implements Command {

    private final UserService userService;
    private final String iban;
    private final double amount;

    public AddFundsCommand(final UserService userService, final String iban,
                           final double amount) {
        this.userService = userService;
        this.iban = iban;
        this.amount = amount;
    }

    /**
     * javadoc
     */
    @Override
    public void execute() {
        try {
            userService.addFundsToAccount(iban, amount);
        } catch (IllegalArgumentException e) {
            System.err.println("Eroare la adăugarea fondurilor: " + e.getMessage());
        }
    }
}
