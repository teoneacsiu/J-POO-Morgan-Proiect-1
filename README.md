# NEACSIU VICTOR-TEODOR 321 CA 2024-2025

# Project - J. POO Morgan Chase & Co - 1st Phase

This project simulates a banking system using object-oriented programming concepts. It features modules to manage users, accounts, and various financial transactions while adhering to core OOP principles.

## File Structure and Class Descriptions

### User and Account Management (`org.poo.model`)

This module handles user accounts and their interactions, such as account creation, transaction management, and reporting.

#### `User.java`
Represents a user of the banking system.
- **Key Attributes**:
    - `email`, `firstName`, `lastName`: Personal details of the user.
    - `accounts`: A list of the user's bank accounts.
    - `transactions`: A record of the user's transactions.
- **Key Methods**:
    - `addTransaction(Transaction transaction)`: Adds a transaction to the user's history.
    - `getIBANForAlias(String alias)`: Resolves an alias to an IBAN.

#### `Account.java`
Base class representing a bank account.
- **Key Attributes**:
    - `iban`, `balance`, `currency`: Core account details.
    - `type`: Specifies the account type (e.g., "classic" or "savings").
- **Key Methods**:
    - `addCard(Card card)`: Associates a card with the account.
    - `setMinBalance(double minBalance)`: Sets the minimum balance for the account.

#### `SavingsAccount.java`
Extends `Account` to represent a savings account.
- **Key Attributes**:
    - `interestRate`: The interest rate for the savings account.

#### `Card.java`
Represents a card associated with a user's account.
- **Key Attributes**:
    - `cardNumber`, `status`, `associatedAccount`: Core card details.
    - `isOneTime`: Indicates whether the card is a one-time-use card.

#### `OneTimeCard.java`
Extends `Card` to represent a one-time-use card.
- **Key Methods**:
    - `useCard()`: Marks the card as used and throws an exception if already used.

---

### Transactions (`org.poo.transactions`)

This module encapsulates various types of financial transactions.

#### `Transaction.java`
Base class for all transactions.
- **Key Attributes**:
    - `description`, `timestamp`, `iban`: Core transaction details.
    - `type`: The type of transaction (`TransactionType`).
- **Key Method**:
    - `toJson()`: Converts transaction details into a JSON representation.

#### `CreateCardTransaction.java`
Extends `Transaction` to represent the creation of a card.
- **Key Attributes**:
    - `cardNum`, `cardHolder`: Details of the card being created.

#### `DeleteCardTransaction.java`
Extends `Transaction` to represent the deletion of a card.
- **Key Attributes**:
    - `cardHolder`, `cardNumber`: Details of the card being deleted.

#### `SendMoneyTransaction.java`
Extends `Transaction` to represent money transfers between accounts.
- **Key Attributes**:
    - `senderIban`, `receiverIban`, `amount`, `currency`: Transfer details.

#### `SplitPaymentTransaction.java`
Extends `Transaction` to represent a payment split among multiple accounts.
- **Key Attributes**:
    - `currency`, `sum`, `accountList`: Details of the split payment.

---

### User Services (`org.poo.service`)

Handles the core logic of user and account management.

#### `UserService.java`
Manages users and their accounts.
- **Key Methods**:
    - `addUser(User user)`: Adds a new user to the system.
    - `addAccount(String email, ...)`: Creates a new account for a user.
    - `deleteAccount(String email, String iban, ...)`: Deletes a user's account.
    - `payOnline(String email, ...)`: Processes an online payment.
    - `generateReport(CommandInput command)`: Creates a transaction report.

---

## Object-Oriented Principles in Practice

### 1. **Encapsulation**
- **Definition**: Encapsulation restricts access to object details, exposing only relevant data and methods.
- **Example**:
    - Fields like `balance` and `iban` in `Account` are private and accessed through public getters/setters.

### 2. **Inheritance**
- **Definition**: Inheritance allows classes to derive properties and methods from a parent class.
- **Example**:
    - `SavingsAccount` inherits from `Account`, adding the `interestRate` field.
    - All transaction types inherit from the base `Transaction` class.

### 3. **Polymorphism**
- **Definition**: Polymorphism allows the same method to behave differently based on the context.
- **Example**:
    - The `toJson()` method is overridden in subclasses of `Transaction` to provide specific JSON representations.

### 4. **Abstraction**
- **Definition**: Abstraction hides implementation details and exposes only essential functionalities.
- **Example**:
    - The `TransactionType` enum abstracts transaction categories.

---

## Observations and Design Characteristics

### **Modularity**
The project employs modular design principles, where each class encapsulates a specific aspect of the system. For instance, `Transaction` serves as the foundation for all transaction types, while `User` and `Account` focus on user and account management, respectively.

### **Extensibility**
The implementation allows for easy addition of new features. For example:
- New transaction types can be added by extending the `Transaction` class and implementing its methods (e.g., `toJson()`).
- Additional account types can inherit from the `Account` class and introduce new fields or methods.

### **Reusability**
Inheritance is used to share functionality across related classes, reducing code duplication. For instance:
- All transaction classes (`CreateCardTransaction`, `SendMoneyTransaction`, etc.) inherit common functionality from the `Transaction` base class.
- Specific account types like `SavingsAccount` build upon the `Account` base class to add new functionality.

### **Consistency**
The use of enums like `TransactionType` ensures that transaction categories are well-defined and prevent errors due to hardcoding. Additionally, uniform interfaces and method naming conventions contribute to a coherent and predictable codebase.

---

## Conclusion

This project represents a solid starting point for the upcoming second phase.
During this phase, minor issues were encountered related to the problem statement
and test logic, but overall, the outcome is satisfactory. As potential improvements,
I could focus on enhancing the `Command` interface (or implementing a Command Pattern),
which would make the command execution process more modularized.
