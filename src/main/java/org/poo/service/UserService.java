package org.poo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.model.*;
import org.poo.transactions.*;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clasă care gestionează utilizatorii și conturile lor.
 */
public class UserService {

    // Lista internă de utilizatori
    private final List<User> users = new ArrayList<>();

    private final CurrencyExchangeService currencyExchangeService;

    public UserService(final CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    /**
     * Adaugă un utilizator nou în sistem.
     *
     * @param user Utilizatorul de adăugat
     */
    public void addUser(final User user) {
        users.add(user);
    }


    /**
     * Găsește un utilizator după email.
     *
     * @param email Email-ul utilizatorului
     * @return Utilizatorul găsit sau null dacă nu există
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
     * javadoc
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
     * javadoc
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
     * Adaugă un cont nou utilizatorului specificat prin email.
     *
     * @param email       Email-ul utilizatorului
     * @param currency    Moneda contului (ex: "RON", "USD")
     * @param accountType Tipul contului: "classic" sau "savings"
     * @param interestRate Dobânda (doar pentru conturile de economii)
     * @return Contul creat
     * @throws IllegalArgumentException Dacă utilizatorul nu este găsit sau tipul de
     * cont este invalid
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
                timestamp);
        newAccount.addTransaction(creationTransaction);

        return newAccount;
    }

    /**
     * Adaugă fonduri într-un cont identificat prin IBAN.
     *
     * @param iban   IBAN-ul contului
     * @param amount Suma de adăugat
     * @throws IllegalArgumentException Dacă IBAN-ul nu există sau suma este invalidă
     */
    public void addFundsToAccount(final String iban, final double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Suma de adăugat trebuie să fie pozitivă.");
        }

        // Căutăm contul după IBAN
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
     * Creează un card pentru un cont specificat prin IBAN.
     *
     * @param email     Email-ul utilizatorului care solicită crearea
     * @param iban      IBAN-ul contului asociat cardului
     * @throws IllegalArgumentException Dacă utilizatorul sau contul
     * nu sunt găsite sau cardType este invalid
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
                timestamp, "New card created");
        account.addTransaction(newTransaction);
    }

    /**
     * javadoc
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
                timestamp, "New card created");
        account.addTransaction(newTransaction);
    }

    /**
     * javadoc
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
     * javadoc
     */
    public void deleteAccount(final String email, final String iban, final int timestamp) {
        // Găsim utilizatorul după email
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        // Găsim contul după IBAN
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        // Verificăm dacă balanța este 0
        if (account.getBalance() != 0.0) {
            account.addTransaction(new Transaction("Account couldn't be deleted - there are funds remaining", timestamp));
            throw new IllegalArgumentException("Account cannot be deleted: balance is not zero.");
        }

        // Ștergem toate cardurile asociate contului
        account.getCards().clear();

        // Ștergem contul din lista utilizatorului
        user.getAccounts().remove(account);

    }

    /**
     * javadoc
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
                account.addTransaction(new DeleteCardTransaction(email, cardNumber, timestamp));
                return;
            }
        }

        throw new IllegalArgumentException("Card not found: " + cardNumber);
    }

    /**
     * javadoc
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
     * javadoc
     */
    public void payOnline(final String email, final String cardNumber,
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
                                    new Transaction("The card is frozen", timestamp);
                            account.addTransaction(newTransaction);
                            return;
                        }

                        // Conversie valutară
                        double convertedAmount = currency.equals(account.getCurrency())
                                ? amount
                                : currencyExchangeService.convert(currency,
                                account.getCurrency(), amount);

                        if (account.getBalance() < convertedAmount) {
                            account.addTransaction(new Transaction("Insufficient funds",
                                    timestamp));
                            return;
                        }

                        if (account.getBalance() - convertedAmount < account.getMinBalance()) {
                            card.setStatus("frozen");
                            Transaction newTransaction =
                                    new Transaction("The card is frozen", timestamp);
                            account.addTransaction(newTransaction);
                            return;
                        }

                        // Debităm balanța
                        account.setBalance(account.getBalance() - convertedAmount);

                        // Adăugăm tranzacție
                        account.addTransaction(new PayOnlineTransaction(convertedAmount,
                                commerciant, "Card payment", timestamp));

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
     * javadoc
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
                            // return "You have reached the minimum amount of funds, the card will be frozen";
                            Transaction newTransaction = new Transaction("You have reached the "
                                    + "minimum amount of funds, the card will be frozen", timestamp);
                            account.addTransaction(newTransaction);
                            return false;
                        }

                        // Frozen case
                        if (balance < minBalance) {
                            card.setStatus("frozen");
                            // return "The card is frozen";
                            Transaction newTransaction = new Transaction("Card is frozen", timestamp);
                            account.addTransaction(newTransaction);
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
     * javadoc
     */
    public void sendMoney(final String senderIBAN, final double amount,
                          final String receiverIBANOrAlias, final int timestamp,
                          final String description, final String senderEmail) {
        // Găsim utilizatorul expeditor
        User senderUser = findUserByEmail(senderEmail);
        if (senderUser == null) {
            return; // Nu facem nimic
        }

        // Găsim contul expeditorului folosind IBAN
        Account senderAccount = findAccountByIBAN(senderIBAN);
        if (senderAccount == null) {
            return; // Nu facem nimic
        }

        // Rezolvăm IBAN-ul destinatarului (alias sau IBAN valid)
        String resolvedReceiverIBAN = resolveAliasGlobally(receiverIBANOrAlias);
        if (resolvedReceiverIBAN == null || resolvedReceiverIBAN.isEmpty()) {
            return; // Dacă IBAN-ul nu este găsit, ieșim fără să scriem nimic
        }

        Account receiverAccount = findAccountByIBAN(resolvedReceiverIBAN);
        if (receiverAccount == null) {
            return; // Dacă contul destinatarului nu există, ieșim fără să scriem nimic
        }

        // Verificăm dacă expeditorul are suficiente fonduri
        if (senderAccount.getBalance() < amount) {
            Transaction newTransaction = new Transaction("Insufficient funds", timestamp);
            senderAccount.addTransaction(newTransaction);
            return;
        }

        // Efectuăm conversia valutară
        double convertedAmount = currencyExchangeService.convert(
                senderAccount.getCurrency(),
                receiverAccount.getCurrency(),
                amount
        );

        // Realizăm transferul
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + convertedAmount);

        senderAccount.addTransaction(new SendMoneyTransaction(senderIBAN, receiverAccount.getIban(),
                amount, senderAccount.getCurrency(), "sent", description, timestamp));
        receiverAccount.addTransaction(new SendMoneyTransaction(senderIBAN, receiverAccount.getIban(),
                convertedAmount, receiverAccount.getCurrency(), "received", description, timestamp));
    }


    /**
     * Metodă care caută un alias în toate aliasurile utilizatorilor
     * și returnează IBAN-ul corespunzător.
     *
     * @param aliasOrIBAN String care poate fi fie un alias, fie un IBAN valid.
     * @return IBAN-ul asociat aliasului dacă este găsit, altfel întoarce aliasOrIBAN inițial.
     */
    private String resolveAliasGlobally(final String aliasOrIBAN) {
        // Verificăm dacă este un alias căutat printre toți utilizatorii
        for (User user : users) {
            String iban = user.getIBANForAlias(aliasOrIBAN);
            if (iban != null) {
                return iban;
            }
        }
        // Dacă nu este un alias, returnăm valoarea inițială (presupunem că e un IBAN)
        return aliasOrIBAN;
    }

    /**
     * javadoc
     */
    public void setAlias(final String email, final String alias,
                         final String accountIBAN) {
        User user = findUserByEmail(email);

        // Verificăm existența utilizatorului
        if (user == null) {
            throw new IllegalArgumentException("Utilizatorul nu există"
                    + "pentru email-ul specificat: " + email);
        }

        // Verificăm dacă IBAN-ul există printre conturile utilizatorului
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(accountIBAN))
                .findFirst()
                .orElse(null);

        if (account == null) {
            throw new IllegalArgumentException("Contul cu IBAN-ul"
                    + "specificat nu există: " + accountIBAN);
        }

        // Adăugăm alias-ul (suprascrie alias-ul existent, dacă este cazul)
        user.getAliases().put(alias, accountIBAN);
    }

    /**
     * javadoc
     */
    public void addInterest(final String iban, final int timestamp) {
        Account account = findAccountByIBAN(iban);

        // Verificăm dacă IBAN-ul este valid
        if (account == null) {
            throw new IllegalArgumentException("Contul cu IBAN-ul specificat nu există: " + iban);
        }

        // Verificăm dacă este un cont de economii
        if (!(account instanceof SavingsAccount savingsAccount)) {
            throw new IllegalArgumentException("This is not a savings account");
        }

        // Calculăm dobânda și o adăugăm la balanță
        double interest = savingsAccount.getBalance() * savingsAccount.getInterestRate() / 100;
        savingsAccount.setBalance(savingsAccount.getBalance() + interest);

        String description = "Interest added: " + interest;
        savingsAccount.addTransaction(new InterestTransaction(savingsAccount.getIban(),
                interest, savingsAccount.getCurrency(), description, timestamp));

    }

    /**
     * javadoc
     */
    public void splitPayment(final CommandInput command) {
        double convertedSplitSum;
        double splitSum = command.getAmount() / command.getAccounts().size();
        String description = "Split payment of " + String.format("%.2f", command.getAmount())
                + " " + command.getCurrency();
        SplitPaymentTransaction newTransaction =
                new SplitPaymentTransaction(command.getTimestamp(), description, command.getCurrency(),
                        splitSum, command.getAccounts());

        List<Account> involvedAccounts = new ArrayList<>();
        boolean hasMoney = true;
        String poorIban = "";
        Account account;

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
                accountt.addTransaction(newTransaction);
            }
        } else {
            newTransaction.setError("Account " + poorIban
                    + " has insufficient funds for a split payment.");
            for (Account accountt : involvedAccounts) {
                accountt.addTransaction(newTransaction);
            }
        }
    }

    /**
     * javadoc
     */
    public Report generateReport(final CommandInput command) {
        Account currAccount = null;
        List<Transaction> transactions = new ArrayList<>();

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                System.out.println("--------------------------------" + account.getIban() + " " + command.getAccount());
                if (command.getAccount().equals(account.getIban())) {
                    currAccount = account;
                }
            }
        }

        if (currAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (currAccount instanceof SavingsAccount) {
            for (Transaction transaction : currAccount.getTransactions()) {
                if (transaction.isInterest()
                        && transaction.getTimestamp() >= command.getStartTimestamp()
                        && transaction.getTimestamp() <= command.getEndTimestamp()) {
                    transactions.add(transaction);
                }
            }
        } else {
            for (Transaction transaction : currAccount.getTransactions()) {
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
}
