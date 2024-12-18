package org.poo.command;

import org.poo.service.UserService;

/**
 * Comandă pentru ștergerea unui card.
 */
public class DeleteCardCommand implements Command {

    private final UserService userService;
    private final String email;
    private final String cardNumber;
    private final int timestamp;

    public DeleteCardCommand(UserService userService, String email, String cardNumber, int timestamp) {
        this.userService = userService;
        this.email = email;
        this.cardNumber = cardNumber;
        this.timestamp = timestamp;
    }

    @Override
    public void execute() {
        try {
            userService.deleteCard(email, cardNumber, timestamp);
        } catch (IllegalArgumentException e) {
            System.err.println("Eroare la ștergerea cardului: " + e.getMessage());
        }
    }
}
