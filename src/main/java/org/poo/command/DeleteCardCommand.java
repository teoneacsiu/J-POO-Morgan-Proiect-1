package org.poo.command;

import org.poo.service.UserService;

/**
 * Command to delete a card.
 */
public class DeleteCardCommand implements Command {

    private final UserService userService;
    private final String email;
    private final String cardNumber;
    private final int timestamp;

    /**
     * Constructs a DeleteCardCommand instance.
     *
     * @param userService the user service to handle the command
     * @param email the email of the user
     * @param cardNumber the card number to be deleted
     * @param timestamp the timestamp of the command
     */
    public DeleteCardCommand(final UserService userService, final String email,
                             final String cardNumber, final int timestamp) {
        this.userService = userService;
        this.email = email;
        this.cardNumber = cardNumber;
        this.timestamp = timestamp;
    }

    @Override
    public void execute() {
        userService.deleteCard(email, cardNumber, timestamp);
    }
}
