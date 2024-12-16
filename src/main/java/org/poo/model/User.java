package org.poo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String firstName;
    private String lastName;
    private String email;

    private List<Account> accounts = new ArrayList<>(); // List of accounts
    private Map<String, String> aliases = new HashMap<>(); // Map to store aliases

    public User() {
        // Default constructor
    }

    public User(String firstName, String lastName, String email) {
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
    public void addAlias(String alias, String iban) {
        aliases.put(alias, iban);
    }

    /**
     * Retrieves the IBAN associated with a given alias.
     *
     * @param alias the alias name
     * @return the IBAN associated with the alias, or null if not found
     */
    public String getIBANForAlias(String alias) {
        return aliases.get(alias);
    }

    /**
     * Checks if an alias exists for the user.
     *
     * @param alias the alias name
     * @return true if the alias exists, false otherwise
     */
    public boolean hasAlias(String alias) {
        return aliases.containsKey(alias);
    }
}
