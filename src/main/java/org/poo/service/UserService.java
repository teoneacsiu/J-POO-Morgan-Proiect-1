package org.poo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingsAccount;
import org.poo.fileio.CommandInput;
import org.poo.model.*;
import org.poo.transactions.*;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that provides functionality for managing users and their accounts.
 */
public class UserService {
    private static final int DIVISOR = 100;

    // List of all users
    private final List<User> users = new ArrayList<>();

    private final CurrencyExchangeService currencyExchangeService;

    public UserService(final CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    /**
     * Adds a new user to the system.
     *
     * @param user the user to be added
     */
    public void addUser(final User user) {
        users.add(user);
    }


    /**
     * Finds a user by their email.
     *
     * @param email the email of the user
     * @return the user found or null if none exists
     */
    public User findUserByEmail(final String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds a user by their IBAN.
     *
     * @param iban the IBAN of the user's account
     * @return the user found or null if none exists
     */
    public User findUserByIban(final String iban) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equalsIgnoreCase(iban)) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Finds an account by its IBAN.
     *
     * @param iban the IBAN of the account
     * @return the account found or null if none exists
     */
    public Account findAccountByIBAN(final String iban) {
        for (User user : users) { // Assuming `users` is a list of all users
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account; // Return the account if IBAN matches
                }
            }
        }
        return null; // Return null if no account with the specified IBAN is found
    }


    /**
     * Finds an account by either its alias or IBAN.
     *
     * @param identifier the alias or IBAN of the account
     * @return the account found or null if none exists
     */
    public Account findAccountByAliasOrIBAN(final String identifier) {
        String resolvedIBAN = identifier; // Assume it's an IBAN by default

        // Check if it's an alias
        for (User user : users) { // Assume `users` is a List<User> in UserService
            String ibanFromAlias = user.getIBANForAlias(identifier);
            if (ibanFromAlias != null) {
                resolvedIBAN = ibanFromAlias;
                break;
            }
        }

        // Find account by IBAN
        return findAccountByIBAN(resolvedIBAN);
    }

    /**
     * Adds a new account to the user specified by their email.
     *
     * @param email       the email of the user
     * @param currency    the currency of the account (e.g., "RON", "USD")
     * @param accountType the type of the account: "classic" or "savings"
     * @param interestRate the interest rate (only for savings accounts)
     * @param timestamp   the timestamp when the account was created
     * @return the created account
     * @throws IllegalArgumentException if the user is not found or the account type is invalid
     */
    public Account addAccount(final String email, final String currency,
                              final String accountType, final Double interestRate,
                              final int timestamp) {
        User user = findUserByEmail(email);

        // Validate user existence
        if (user == null) {
            throw new IllegalArgumentException("User not found for email: " + email);
        }

        // Generate a unique IBAN
        String iban = Utils.generateIBAN();
        Account newAccount;

        // Create the account based on type
        if ("classic".equalsIgnoreCase(accountType)) {
            newAccount = new Account(iban, currency, "classic");
        } else if ("savings".equalsIgnoreCase(accountType)) {
            if (interestRate == null) {
                throw new IllegalArgumentException("Interest rate is required "
                        + "for savings accounts.");
            }
            newAccount = new SavingsAccount(iban, currency, interestRate);
        } else {
            throw new IllegalArgumentException("Invalid account type: " + accountType);
        }

        // Ensure account list is initialized
        if (user.getAccounts() == null) {
            user.setAccounts(new ArrayList<>());
        }

        // Add the account to the user
        user.getAccounts().add(newAccount);

        // Add a transaction for account creation
        Transaction creationTransaction = new Transaction("New account created",
                timestamp, iban);
        user.addTransaction(creationTransaction);

        return newAccount;
    }

    /**
     * Adds funds to an account identified by its IBAN.
     *
     * @param iban   the IBAN of the account
     * @param amount the amount to be added
     * @throws IllegalArgumentException if the IBAN does not exist or the amount is invalid
     */
    public void addFundsToAccount(final String iban, final double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Suma de adăugat trebuie să fie pozitivă.");
        }

        // Find the account by IBAN
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    account.setBalance(account.getBalance() + amount);
                    return;
                }
            }
        }

        throw new IllegalArgumentException("IBAN-ul specificat nu există: " + iban);
    }

    /**
     * Creates a card for an account specified by its IBAN.
     *
     * @param email     the email of the user requesting the card creation
     * @param iban      the IBAN of the account associated with the card
     * @param timestamp the timestamp when the card was created
     * @throws IllegalArgumentException if the user or account is not found
     */
    public void createCardForAccount(final String email, final String iban,
                                     final int timestamp) {
        User user = findUserByEmail(email);
        if (user == null) {
            return;
        }

        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        String cardNumber = Utils.generateCardNumber();
        Card card = new Card(cardNumber, iban);
        account.addCard(card);
        Transaction newTransaction = new CreateCardTransaction(cardNumber, email, iban,
                timestamp, "New card created", account.getIban());
        user.addTransaction(newTransaction);
    }

    /**
     * Creates a one-time card for a specified account identified by its IBAN.
     *
     * @param email     the email of the user requesting the one-time card
     * @param iban      the IBAN of the account associated with the one-time card
     * @param timestamp the timestamp when the card was created
     * @throws IllegalArgumentException if the user or account is not found
     */
    public void createOneTimeCard(final String email, final String iban,
                                  final int timestamp) {
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        String cardNumber = Utils.generateCardNumber();
        OneTimeCard oneTimeCard = new OneTimeCard(cardNumber, iban);
        account.addCard(oneTimeCard);
        Transaction newTransaction = new CreateCardTransaction(cardNumber, email, iban,
                timestamp, "New card created", account.getIban());
        user.addTransaction(newTransaction);
    }

    /**
     * Generates a snapshot of all users and their associated accounts.
     *
     * @param objectMapper the JSON object mapper to structure the snapshot
     * @return an ArrayNode representing the snapshot of users
     */
    public ArrayNode getUsersSnapshot(final ObjectMapper objectMapper) {
        ArrayNode usersArray = objectMapper.createArrayNode();
        for (User user : users) {
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());

            // Add accounts
            ArrayNode accountsArray = objectMapper.createArrayNode();
            for (Account account : user.getAccounts()) {
                ObjectNode accountNode = objectMapper.createObjectNode();
                accountNode.put("IBAN", account.getIban());
                accountNode.put("balance", account.getBalance());
                accountNode.put("currency", account.getCurrency());
                accountNode.put("type", account.getType());

                // Add cards associated with the account
                ArrayNode cardsArray = objectMapper.createArrayNode();
                for (Card card : account.getCards()) {
                    ObjectNode cardNode = objectMapper.createObjectNode();
                    cardNode.put("cardNumber", card.getCardNumber());
                    cardNode.put("status", card.getStatus());
                    cardsArray.add(cardNode);
                }
                accountNode.set("cards", cardsArray); // Attach cards to account
                accountsArray.add(accountNode);
            }

            userNode.set("accounts", accountsArray); // Attach accounts to user
            usersArray.add(userNode);
        }
        return usersArray;
    }

    /**
     * Deletes an account identified by its IBAN for a specified user.
     *
     * @param email     the email of the user owning the account
     * @param iban      the IBAN of the account to be deleted
     * @param timestamp the timestamp of the deletion
     * @throws IllegalArgumentException if the user or account is not found, or if the balance is not zero
     */
    public void deleteAccount(final String email, final String iban, final int timestamp) {
        // Find the user by email
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        // Find the account by IBAN
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        // Check if the account has a balance different from zero
        if (account.getBalance() != 0.0) {
            user.addTransaction(new Transaction("Account couldn't be deleted -"
                    + " there are funds remaining",
                    timestamp, account.getIban()));
            throw new IllegalArgumentException("Account cannot be deleted: balance is not zero.");
        }

        // Delete all cards associated with the account
        account.getCards().clear();

        // Delete the account
        user.getAccounts().remove(account);

    }

    /**
     * Deletes a card identified by its card number for a specified user.
     *
     * @param email      the email of the user owning the card
     * @param cardNumber the card number to be deleted
     * @param timestamp  the timestamp of the deletion
     * @throws IllegalArgumentException if the user or card is not found, or the card number is invalid
     */
    public void deleteCard(final String email, final String cardNumber,
                           final int timestamp) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            throw new IllegalArgumentException("Invalid card number: " + cardNumber);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        for (Account account : user.getAccounts()) {
            Card cardToDelete = account.getCards().stream()
                    .filter(card -> card.getCardNumber().equals(cardNumber))
                    .findFirst()
                    .orElse(null);

            if (cardToDelete != null) {
                account.getCards().remove(cardToDelete);
                user.addTransaction(new DeleteCardTransaction(email, cardNumber,
                        timestamp, account.getIban()));
            }
        }
    }

    /**
     * Sets the minimum balance for an account identified by its IBAN.
     *
     * @param iban       the IBAN of the account
     * @param minBalance the minimum balance to set for the account
     * @throws IllegalArgumentException if the account is not found
     */
    public void setMinBalance(final String iban, final double minBalance) {
        // Find the account by IBAN
        Account account = findAccountByIBAN(iban);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + iban);
        }

        // Set the minimum balance for the account
        account.setMinBalance(minBalance);
    }

    /**
     * Processes an online payment using a specific card.
     *
     * @param email       the email of the user making the payment
     * @param cardNumber  the card number to be used for the payment
     * @param amount      the amount to be paid
     * @param currency    the currency of the payment
     * @param timestamp   the timestamp of the transaction
     * @param commerciant the commerciant receiving the payment
     * @throws IllegalArgumentException if the user, card, or account is not found,
     *                                  or if there are insufficient funds
     */
    public void payOnline(final String email, String cardNumber,
                          final double amount, final String currency,
                          final int timestamp, final String commerciant) {
        try {
            User user = findUserByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + email);
            }

            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        if (card.getStatus().equalsIgnoreCase("frozen")) {
                            Transaction newTransaction =
                                    new Transaction("The card is frozen", timestamp,
                                            account.getIban());
                            user.addTransaction(newTransaction);
                            return;
                        }

                        // Value conversion
                        double convertedAmount = currency.equals(account.getCurrency())
                                ? amount
                                : currencyExchangeService.convert(currency,
                                account.getCurrency(), amount);

                        if (account.getBalance() < convertedAmount) {
                            user.addTransaction(new Transaction("Insufficient funds",
                                    timestamp, account.getIban()));
                            return;
                        }

                        if (account.getBalance() - convertedAmount < account.getMinBalance()) {
                            card.setStatus("frozen");
                            Transaction newTransaction =
                                    new Transaction("The card is frozen", timestamp,
                                            account.getIban());
                            user.addTransaction(newTransaction);
                            return;
                        }

                        // Update balance
                        account.setBalance(account.getBalance() - convertedAmount);

                        // Add transaction
                        user.addTransaction(new PayOnlineTransaction(convertedAmount,
                                commerciant, "Card payment", timestamp, account.getIban()));

                        if (card.isOneTime()) {
                            regenOneTimeCard(account, card, email, timestamp);
                        }

                        return;
                    }
                }
            }
            throw new IllegalArgumentException("Card not found");
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Checks the status of a card and determines if it should be frozen.
     *
     * @param cardNumber the card number to check
     * @param timestamp  the timestamp of the check
     * @return true if the card is in good standing, false otherwise
     */
    public boolean checkCardStatus(final String cardNumber, final int timestamp) {
        // Search through all users and their accounts to find the card
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        double balance = account.getBalance();
                        double minBalance = account.getMinBalance();

                        // Warning case
                        if ((balance - minBalance) <= 30) {
                            Transaction newTransaction = new Transaction("You have reached the "
                                    + "minimum amount of funds, the card will be frozen",
                                    timestamp, account.getIban());
                            user.addTransaction(newTransaction);
                            return false;
                        }

                        // Frozen case
                        if (balance < minBalance) {
                            card.setStatus("frozen");
                            Transaction newTransaction = new Transaction("Card is frozen",
                                    timestamp, account.getIban());
                            user.addTransaction(newTransaction);
                            return false;
                        }
                        return false;
                    }
                }

            }
        }
        return true;
    }

    /**
     * Transfers money from one account to another.
     *
     * @param senderIBAN          the IBAN of the sender's account
     * @param amount              the amount to transfer
     * @param receiverIBANOrAlias the IBAN or alias of the receiver's account
     * @param timestamp           the timestamp of the transaction
     * @param description         the description of the transaction
     * @param senderEmail         the email of the sender
     */
    public void sendMoney(final String senderIBAN, final double amount,
                          final String receiverIBANOrAlias, final int timestamp,
                          final String description, final String senderEmail) {
        // Find the user and account of the sender
        User senderUser = findUserByEmail(senderEmail);
        if (senderUser == null) {
            return;
        }

        // Find the account of the sender
        Account senderAccount = findAccountByIBAN(senderIBAN);
        if (senderAccount == null) {
            return;
        }

        // Resolve the receiver's IBAN from the alias
        String resolvedReceiverIBAN = resolveAliasGlobally(receiverIBANOrAlias);
        if (resolvedReceiverIBAN == null || resolvedReceiverIBAN.isEmpty()) {
            return;
        }

        Account receiverAccount = findAccountByIBAN(resolvedReceiverIBAN);
        User receiverUser = findUserByIban(resolvedReceiverIBAN);
        if (receiverAccount == null) {
            return;
        }

        // Verify if the sender has enough funds
        if (senderAccount.getBalance() < amount) {
            Transaction newTransaction = new Transaction("Insufficient funds",
                    timestamp, senderAccount.getIban());
            senderUser.addTransaction(newTransaction);
            return;
        }

        // Convert the amount to the receiver's currency
        double convertedAmount = currencyExchangeService.convert(
                senderAccount.getCurrency(),
                receiverAccount.getCurrency(),
                amount
        );

        // Do the transaction
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + convertedAmount);

        senderUser.addTransaction(new SendMoneyTransaction(senderIBAN, receiverAccount.getIban(),
                amount, senderAccount.getCurrency(), "sent", description, timestamp, senderIBAN));
        receiverUser.addTransaction(new SendMoneyTransaction(senderIBAN, receiverAccount.getIban(),
                convertedAmount, receiverAccount.getCurrency(), "received", description,
                timestamp, resolvedReceiverIBAN));
    }


    /**
     * Resolves an alias to its corresponding IBAN.
     *
     * @param aliasOrIBAN a string that can either be an alias or a valid IBAN
     * @return the IBAN associated with the alias if found, otherwise returns the original input
     */
    private String resolveAliasGlobally(final String aliasOrIBAN) {
        // Check if the input is an alias
        for (User user : users) {
            String iban = user.getIBANForAlias(aliasOrIBAN);
            if (iban != null) {
                return iban;
            }
        }
        // If the input is not an alias, return the original input
        return aliasOrIBAN;
    }

    /**
     * Sets an alias for an account identified by its IBAN.
     *
     * @param email       the email of the user
     * @param alias       the alias to set
     * @param accountIBAN the IBAN of the account
     * @throws IllegalArgumentException if the user or account is not found
     */
    public void setAlias(final String email, final String alias,
                         final String accountIBAN) {
        User user = findUserByEmail(email);

        // Verify if the user exists
        if (user == null) {
            throw new IllegalArgumentException("Utilizatorul nu există"
                    + "pentru email-ul specificat: " + email);
        }

        // Verify if the account exists
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(accountIBAN))
                .findFirst()
                .orElse(null);

        if (account == null) {
            throw new IllegalArgumentException("Contul cu IBAN-ul"
                    + "specificat nu există: " + accountIBAN);
        }

        // Add the alias to the user
        user.getAliases().put(alias, accountIBAN);
    }

    /**
     * Adds interest to a savings account identified by its IBAN.
     *
     * @param iban the IBAN of the account
     * @throws IllegalArgumentException if the account is not found or is not a savings account
     */
    public void addInterest(final String iban) {
        Account account = findAccountByIBAN(iban);

        // Verify if the account exists
        if (account == null) {
            throw new IllegalArgumentException("Contul cu IBAN-ul specificat nu există: " + iban);
        }

        // Verify if the account is a savings account
        if (!account.getType().equalsIgnoreCase("savings")) {
            throw new IllegalArgumentException("This is not a savings account");
        }

        // Add interest to the account
        SavingsAccount savingsAccount = (SavingsAccount) account;
        double interest = savingsAccount.getBalance() * savingsAccount.getInterestRate() / DIVISOR;
        savingsAccount.setBalance(savingsAccount.getBalance() + interest);
    }

    /**
     * Splits a payment among multiple accounts.
     *
     * @param command the command input containing details of the split payment
     */
    public void splitPayment(final CommandInput command) {
        double convertedSplitSum;
        double splitSum = command.getAmount() / command.getAccounts().size();
        String description = "Split payment of " + String.format("%.2f", command.getAmount())
                + " " + command.getCurrency();
        SplitPaymentTransaction newTransaction =
                new SplitPaymentTransaction(command.getTimestamp(), description,
                        command.getCurrency(), splitSum, command.getAccounts());

        Account account;
        List<Account> involvedAccounts = new ArrayList<>();


        boolean hasMoney = true;
        String poorIban = "";

        for (String iban : command.getAccounts()) {
            account = findAccountByIBAN(iban);
            involvedAccounts.add(account);

            convertedSplitSum = currencyExchangeService.convert(command.getCurrency(),
                    account.getCurrency(), splitSum);

            if (account.getBalance() < convertedSplitSum) {
                hasMoney = false;
                poorIban = account.getIban();
            }
        }

        if (hasMoney) {
            for (Account accountt : involvedAccounts) {
                convertedSplitSum = currencyExchangeService.convert(command.getCurrency(),
                        accountt.getCurrency(), splitSum);

                accountt.setBalance(accountt.getBalance() - convertedSplitSum);
                newTransaction.setIban(accountt.getIban());

                User user = findUserByIban(accountt.getIban());

                user.addTransaction(newTransaction);
            }
        } else {
            newTransaction.setError("Account " + poorIban
                    + " has insufficient funds for a split payment.");
            for (Account accountt : involvedAccounts) {
                newTransaction.setIban(accountt.getIban());

                User user = findUserByIban(accountt.getIban());

                user.addTransaction(newTransaction);
            }

        }
    }

    /**
     * Generates a report of transactions for a specific account.
     *
     * @param command the command input specifying the account and time range
     * @return a report containing the transactions, balance, and currency of the account
     * @throws IllegalArgumentException if the account is not found
     */
    public Report generateReport(final CommandInput command) {
        Account currAccount = findAccountByIBAN(command.getAccount());
        List<Transaction> transactions = new ArrayList<>();

        if (currAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        User currUser = findUserByIban(currAccount.getIban());

        if (currAccount.getType().equalsIgnoreCase("savings")) {

            for (Transaction transaction : currUser.getTransactions()) {
                if (transaction.getType() == TransactionType.INTEREST
                        && transaction.getTimestamp() >= command.getStartTimestamp()
                        && transaction.getTimestamp() <= command.getEndTimestamp()) {
                    transactions.add(transaction);
                }
            }
        } else {
            for (Transaction transaction : currUser.getTransactions()) {
                if (transaction.getTimestamp() >= command.getStartTimestamp()
                        && transaction.getTimestamp() <= command.getEndTimestamp()) {
                    transactions.add(transaction);
                }
            }
        }

        // Include balance and currency in the result
        double balance = currAccount.getBalance(); // Assuming Account has getBalance()
        String currency = currAccount.getCurrency(); // Assuming Account has getCurrency()

        return new Report(transactions, balance, currency);
    }

    /**
     * Generates a spending report for an account.
     *
     * @param command the command input specifying the account and time range
     * @return a spending report containing the transactions, balance, and currency
     * @throws IllegalArgumentException if the account is not found or is a savings account
     */
    public SpendingsReport generateSpendingsReport(final CommandInput command) {
        Account currAccount = findAccountByIBAN(command.getAccount());
        User user = findUserByIban(command.getAccount());
        List<PayOnlineTransaction> transactions = new ArrayList<>();

        if (currAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (currAccount.getType().equalsIgnoreCase("savings")) {
            throw new IllegalArgumentException("This kind of report is not "
                    + "supported for a saving account");
        }

        for (Transaction transaction : user.getTransactions()) {
            if (transaction.getType() == TransactionType.PAY_ONLINE
                    && transaction.getIban().equalsIgnoreCase(command.getAccount())
                    && transaction.getTimestamp() >= command.getStartTimestamp()
                    && transaction.getTimestamp() <= command.getEndTimestamp()) {
                transactions.add((PayOnlineTransaction) transaction);
            }
        }

        return new SpendingsReport(transactions, currAccount.getBalance(),
                currAccount.getCurrency());
    }

    /**
     * Regenerates a one-time card for a specific account.
     *
     * @param account   the account associated with the card
     * @param card      the one-time card to regenerate
     * @param email     the email of the user requesting the regeneration
     * @param timestamp the timestamp of the regeneration
     */
    public void regenOneTimeCard(final Account account, final Card card,
                                 final String email, final int timestamp) {
        account.getCards().remove(card);
        String cardNumber = Utils.generateCardNumber();
        OneTimeCard oneTimeCard = new OneTimeCard(cardNumber, account.getIban());
        account.addCard(oneTimeCard);
        User user = findUserByIban(account.getIban());
        user.addTransaction(new DeleteCardTransaction(email, card.getCardNumber(),
                timestamp, account.getIban()));
        user.addTransaction(new CreateCardTransaction(oneTimeCard.getCardNumber(), email,
                account.getIban(), timestamp, "New card created", account.getIban()));
    }

    /**
     * Changes the interest rate of a savings account.
     *
     * @param commandInput the command input specifying the account and new interest rate
     * @throws IllegalArgumentException if the account is not found or is not a savings account
     */
    public void changeInterestRate(final CommandInput commandInput) {
        Account account = findAccountByIBAN(commandInput.getAccount());

        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (!account.getType().equalsIgnoreCase("savings")) {
            throw new IllegalArgumentException("This is not a savings account");
        }

        User user = findUserByIban(commandInput.getAccount());

        SavingsAccount savingsAccount = (SavingsAccount) account;
        savingsAccount.setInterestRate(commandInput.getInterestRate());
        user.addTransaction(new Transaction("Interest rate of the account changed to "
                + commandInput.getInterestRate(), commandInput.getTimestamp()));
    }
}
