package org.poo.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String firstName;
    private String lastName;
    private String email;

    private List<Account> accounts = new ArrayList<>(); // INIȚIALIZARE

    public User() {
        // Constructor implicit fără alte modificări
    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}

