package org.poo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.model.*;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasă care gestionează utilizatorii și conturile lor.
 */
public class UserService {

    // Lista internă de utilizatori
    private final List<User> users = new ArrayList<>();

    private final CurrencyExchangeService currencyExchangeService;

    public UserService(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    /**
     * Adaugă un utilizator nou în sistem.
     *
     * @param user Utilizatorul de adăugat
     */
    public void addUser(User user) {
        users.add(user);
        System.out.println("Utilizator adăugat: " + user.getEmail());
    }


    /**
     * Găsește un utilizator după email.
     *
     * @param email Email-ul utilizatorului
     * @return Utilizatorul găsit sau null dacă nu există
     */
    public User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public Account findAccountByIBAN(String iban) {
        for (User user : users) { // Assuming `users` is a list of all users
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account; // Return the account if IBAN matches
                }
            }
        }
        return null; // Return null if no account with the specified IBAN is found
    }

    public Account findAccountByAliasOrIBAN(String identifier) {
        String resolvedIBAN = identifier; // Assume it's an IBAN by default

        // Check if it's an alias
        for (User user : users) { // Assume `users` is a List<User> in UserService
            String ibanFromAlias = user.getIBANForAlias(identifier); // Implement getIBANForAlias in User
            if (ibanFromAlias != null) {
                resolvedIBAN = ibanFromAlias;
                break;
            }
        }

        // Find account by IBAN
        return findAccountByIBAN(resolvedIBAN);
    }

    public String resolveAliasOrIBAN(String identifier, User senderUser) {
        // Check if the identifier is an alias in the sender's alias list
        String resolvedIBAN = senderUser.getIBANForAlias(identifier);
        if (resolvedIBAN != null) {
            return resolvedIBAN; // Alias found, return IBAN
        }

        // Otherwise, assume the identifier is an IBAN
        return identifier;
    }



    /**
     * Adaugă un cont nou utilizatorului specificat prin email.
     *
     * @param email       Email-ul utilizatorului
     * @param currency    Moneda contului (ex: "RON", "USD")
     * @param accountType Tipul contului: "classic" sau "savings"
     * @param interestRate Dobânda (doar pentru conturile de economii)
     * @return Contul creat
     * @throws IllegalArgumentException Dacă utilizatorul nu este găsit sau tipul de cont este invalid
     */
    public Account addAccount(String email, String currency, String accountType, Double interestRate, int timestamp) {
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
                throw new IllegalArgumentException("Interest rate is required for savings accounts.");
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
        Transaction creationTransaction = new Transaction(timestamp, "New account created");
        newAccount.addTransaction(creationTransaction);

        return newAccount;
    }



    /**
     * Returnează lista tuturor utilizatorilor din sistem.
     *
     * @return Lista de utilizatori
     */
    public List<User> getAllUsers() {
        return users;
    }

    /**
     * Adaugă fonduri într-un cont identificat prin IBAN.
     *
     * @param iban   IBAN-ul contului
     * @param amount Suma de adăugat
     * @throws IllegalArgumentException Dacă IBAN-ul nu există sau suma este invalidă
     */
    public void addFundsToAccount(String iban, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Suma de adăugat trebuie să fie pozitivă.");
        }

        // Căutăm contul după IBAN
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    account.setBalance(account.getBalance() + amount);
                    System.out.println("Fonduri adăugate: " + amount + " în contul cu IBAN-ul: " + iban);
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
     * @param cardType  Tipul cardului: "regular" sau "one-time"
     * @return Cardul creat
     * @throws IllegalArgumentException Dacă utilizatorul sau contul nu sunt găsite sau cardType este invalid
     */
    public void createCardForAccount(String email, String iban, String cardType) {
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + iban));

        String cardNumber = Utils.generateCardNumber();
        Card card = new Card(cardNumber, iban);
        account.addCard(card);
    }

    public void createOneTimeCard(String email, String iban) {
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
    }


    public ArrayNode getUsersSnapshot(ObjectMapper objectMapper) {
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

    public void deleteAccount(String email, String iban) {
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
            throw new IllegalArgumentException("Account cannot be deleted: balance is not zero.");
        }

        // Ștergem toate cardurile asociate contului
        account.getCards().clear();

        // Ștergem contul din lista utilizatorului
        user.getAccounts().remove(account);

        System.out.println("Account with IBAN " + iban + " deleted for user " + email);
    }

    public void deleteCard(String email, String cardNumber, int timestamp) {
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
                account.addTransaction(new Transaction(timestamp, "Card " + cardNumber + " deleted"));
                return;
            }
        }

        throw new IllegalArgumentException("Card not found: " + cardNumber);
    }

    public void setMinBalance(String iban, double minBalance, int timestamp) {
        // Find the account by IBAN
        Account account = findAccountByIBAN(iban);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + iban);
        }

        // Set the minimum balance for the account
        account.setMinBalance(minBalance);

        // Add a transaction to record the operation
        account.addTransaction(new Transaction(timestamp, "Minimum balance set to " + minBalance));
    }

    public void payOnline(String email, String cardNumber, double amount, String currency, int timestamp, String description, String commerciant) {
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0.");
            }

            User user = findUserByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + email);
            }

            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        if (card.getStatus().equalsIgnoreCase("frozen")) {
                            throw new IllegalArgumentException("Card is frozen: " + cardNumber);
                        }

                        double convertedAmount = amount;
                        if (!account.getCurrency().equalsIgnoreCase(currency)) {
                            convertedAmount = currencyExchangeService.convert(currency, account.getCurrency(), amount);
                        }

                        if (account.getBalance() < convertedAmount) {
                            account.addTransaction(new Transaction(
                                    timestamp,
                                    "Insufficient funds for payment: " + description
                            ));
                            return;
                        }


                        account.setBalance(account.getBalance() - convertedAmount);
                        account.addTransaction(new Transaction(timestamp, "Payment to " + commerciant + ": " + description + ", amount: " + amount + " " + currency));
                        if (card instanceof OneTimeCard) {
                            ((OneTimeCard) card).setUsed(true);
                        }
                        return; // Exit after successful payment
                    }
                }
            }
            throw new IllegalArgumentException("Card not found");
        } catch (Exception e) {
            throw new IllegalArgumentException("" + e.getMessage());
        }
    }

    public String checkCardStatus(String cardNumber, int timestamp) {
        // Search through all users and their accounts to find the card
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        double balance = account.getBalance();
                        double minBalance = account.getMinBalance();

                        // Frozen case
                        if (balance <= minBalance) {
                            card.setStatus("frozen");
                            return "frozen";
                        }

                        // Warning case
                        if ((balance - minBalance) <= 30) {
                            return "warning";
                        }

                        // Active case
                        return "active";
                    }
                }
            }
        }

        // If card not found, throw an exception
        throw new IllegalArgumentException("Card not found: " + cardNumber);
    }

    public void sendMoney(String senderIBANOrAlias, double amount, String receiverIBANOrAlias,
                          int timestamp, String description, String senderEmail) {
        // Find the sender user
        User senderUser = findUserByEmail(senderEmail);
        if (senderUser == null) {
            return;
        }

        // Resolve sender IBAN
        boolean isSenderAlias = senderUser.hasAlias(senderIBANOrAlias);
        String resolvedSenderIBAN = isSenderAlias ? senderUser.getIBANForAlias(senderIBANOrAlias) : senderIBANOrAlias;

        Account senderAccount = findAccountByIBAN(resolvedSenderIBAN);
        if (senderAccount == null) {
            return;
        }

        // Resolve receiver IBAN
        boolean isReceiverAlias = senderUser.hasAlias(receiverIBANOrAlias);
        String resolvedReceiverIBAN = isReceiverAlias ? senderUser.getIBANForAlias(receiverIBANOrAlias) : receiverIBANOrAlias;

        Account receiverAccount = findAccountByIBAN(resolvedReceiverIBAN);
        if (receiverAccount == null) {
            return;
        }

        // Block transactions if one uses an alias and the other uses an IBAN
        if ((isSenderAlias && !isReceiverAlias) || (!isSenderAlias && isReceiverAlias)) {
            return;
        }

        // Check if sender has sufficient funds
        if (senderAccount.getBalance() < amount) {
            return;
        }

        // Perform currency conversion
        double convertedAmount = currencyExchangeService.convert(
                senderAccount.getCurrency(),
                receiverAccount.getCurrency(),
                amount
        );

        // Perform the transfer
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + convertedAmount);

        // Log the transaction for both accounts
        senderAccount.addTransaction(new Transaction(
                timestamp,
                description,
                senderAccount.getIban(),
                receiverAccount.getIban(),
                amount,
                senderAccount.getCurrency(),
                "sent"
        ));
        receiverAccount.addTransaction(new Transaction(
                timestamp,
                description,
                senderAccount.getIban(),
                receiverAccount.getIban(),
                convertedAmount,
                receiverAccount.getCurrency(),
                "received"
        ));
    }



    private boolean isIBAN(String receiverIBANOrAlias) {
        return receiverIBANOrAlias.matches("^[A-Z]{2}[0-9]{2}[A-Z]{4}[0-9]{16}$");
    }


    private User findUserByIBAN(String senderIBAN) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(senderIBAN)) {
                    return user;
                }
            }
        }
        return null;
    }


    public void setAlias(String email, String alias, String accountIBAN) {
        User user = findUserByEmail(email);

        // Verificăm existența utilizatorului
        if (user == null) {
            throw new IllegalArgumentException("Utilizatorul nu există pentru email-ul specificat: " + email);
        }

        // Verificăm dacă IBAN-ul există printre conturile utilizatorului
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getIban().equals(accountIBAN))
                .findFirst()
                .orElse(null);

        if (account == null) {
            throw new IllegalArgumentException("Contul cu IBAN-ul specificat nu există: " + accountIBAN);
        }

        // Adăugăm alias-ul (suprascrie alias-ul existent, dacă este cazul)
        user.getAliases().put(alias, accountIBAN);
    }


    public List<Transaction> getTransactions(String email) {
        User user = findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : user.getAccounts()) {
            allTransactions.addAll(account.getTransactions());
        }

        return allTransactions;
    }

    public void addInterest(String iban, int timestamp) {
        Account account = findAccountByIBAN(iban);

        // Verificăm dacă IBAN-ul este valid
        if (account == null) {
            throw new IllegalArgumentException("Contul cu IBAN-ul specificat nu există: " + iban);
        }

        // Verificăm dacă este un cont de economii
        if (!(account instanceof SavingsAccount)) {
            throw new IllegalArgumentException("Dobânda poate fi aplicată doar conturilor de economii.");
        }

        SavingsAccount savingsAccount = (SavingsAccount) account;

        // Calculăm dobânda și o adăugăm la balanță
        double interest = savingsAccount.getBalance() * savingsAccount.getInterestRate() / 100;
        savingsAccount.setBalance(savingsAccount.getBalance() + interest);

        // Înregistrăm tranzacția
        savingsAccount.addTransaction(new Transaction(
                timestamp,
                "Interest added: " + interest,
                null,
                savingsAccount.getIban(),
                interest,
                savingsAccount.getCurrency(),
                "interest"
        ));
    }

    public boolean isAlias(String senderIBAN) {
        for (User user : users) {
            if (user.hasAlias(senderIBAN)) {
                return true;
            }
        }
        return false;
    }
}
