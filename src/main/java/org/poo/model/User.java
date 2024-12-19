package org.poo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.transactions.Transaction;

@Getter
@Setter
public class User {
    private String firstName;
    private String lastName;
    private String email;

    private List<Account> accounts = new ArrayList<>(); // List of accounts
    private Map<String, String> aliases = new HashMap<>(); // Map to store aliases
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Constructs a User instance.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email of the user
     */
    public User(final String firstName, final String lastName,
                final String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Adds an alias for a specific IBAN.
     *
     * @param alias the alias name
     * @param iban the IBAN associated with the alias
     */
    public void addAlias(final String alias, final String iban) {
        aliases.put(alias, iban);
    }

    /**
     * Retrieves the IBAN associated with a given alias.
     *
     * @param alias the alias name
     * @return the IBAN associated with the alias, or null if not found
     */
    public String getIBANForAlias(final String alias) {
        return aliases.get(alias);
    }

    /**
     * Checks if an alias exists for the user.
     *
     * @param alias the alias name
     * @return true if the alias exists, false otherwise
     */
    public boolean hasAlias(final String alias) {
        return aliases.containsKey(alias);
    }

    /**
     * Add a transaction to the account
     *
     * @param transaction the transaction to be added
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }
}
