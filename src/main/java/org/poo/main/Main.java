package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.model.ExchangeRate;
import org.poo.model.User;
import org.poo.service.CurrencyExchangeService;
import org.poo.service.UserService;
import org.poo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        var sortedFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles())).
                sorted(Comparator.comparingInt(Main::fileConsumer))
                .toList();

        for (File file : sortedFiles) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1, final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(CheckerConstants.TESTS_PATH + filePath1);
        ObjectInput inputData = objectMapper.readValue(file, ObjectInput.class);

        // Reset random generators
        Utils.resetRandom();

        // Initialize CurrencyExchangeService
        CurrencyExchangeService currencyExchangeService = new CurrencyExchangeService();

        // Load exchange rates
        for (ExchangeInput exchangeInput : inputData.getExchangeRates()) {
            currencyExchangeService.addExchangeRate(exchangeInput.getFrom(), exchangeInput.getTo(), exchangeInput.getRate());
        }

        // Initialize UserService and pass CurrencyExchangeService instance
        UserService userService = new UserService(currencyExchangeService);

        // Create output array
        ArrayNode output = objectMapper.createArrayNode();

        // Add users from input
        for (var userInput : inputData.getUsers()) {
            userService.addUser(new User(userInput.getFirstName(), userInput.getLastName(), userInput.getEmail()));
        }

        // Process commands
        for (var command : inputData.getCommands()) {
            try {
                switch (command.getCommand()) {
                    case "printUsers" -> {
                        // Create and add the snapshot of users to output
                        var printNode = objectMapper.createObjectNode();
                        printNode.put("command", "printUsers");
                        printNode.set("output", userService.getUsersSnapshot(objectMapper));
                        printNode.put("timestamp", command.getTimestamp());
                        output.add(printNode);
                    }
                    case "addAccount" -> {
                        // Perform the add account operation
                        userService.addAccount(
                                command.getEmail(),
                                command.getCurrency(),
                                command.getAccountType(),
                                command.getInterestRate()
                        );
                    }
                    case "addFunds" -> {
                        // Perform the add funds operation
                        userService.addFundsToAccount(
                                command.getAccount(),
                                command.getAmount()
                        );
                    }
                    case "createCard" -> {
                        // Perform the create card operation
                        userService.createCardForAccount(
                                command.getEmail(),
                                command.getAccount(),
                                "regular"
                        );
                    }
                    case "deleteAccount" -> {
                        userService.deleteAccount(
                                command.getEmail(),
                                command.getAccount()
                        );

                        // Add success message to the output
                        var successNode = objectMapper.createObjectNode();
                        successNode.put("command", "deleteAccount");
                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("success", "Account deleted");
                        outputNode.put("timestamp", command.getTimestamp());
                        successNode.set("output", outputNode);
                        successNode.put("timestamp", command.getTimestamp());
                        output.add(successNode);
                    }
                    case "createOneTimeCard" -> {
                        userService.createOneTimeCard(
                                command.getEmail(),
                                command.getAccount()
                        );
                    }
                    case "deleteCard" -> {
                        try {
                            // Extract card number using Lombok-generated getter
                            String cardNumber = command.getCardNumber();

                            userService.deleteCard(
                                    command.getEmail(),
                                    cardNumber,           // Use card number
                                    command.getTimestamp()
                            );


                        } catch (Exception e) {
                            var errorNode = objectMapper.createObjectNode();
                            errorNode.put("status", "error");
                            errorNode.put("command", "deleteCard");
                            errorNode.put("message", e.getMessage());
                            output.add(errorNode);
                        }
                    }
                    case "setMinimumBalance" -> {
                        try {
                            userService.setMinBalance(
                                    command.getAccount(),   // IBAN of the account
                                    command.getAmount(),    // Minimum balance to set
                                    command.getTimestamp()  // Timestamp of the operation
                            );
                        } catch (Exception e) {
                            var errorNode = objectMapper.createObjectNode();
                            errorNode.put("status", "error");
                            errorNode.put("command", "setMinBalance");
                            errorNode.put("message", e.getMessage());
                            output.add(errorNode);
                        }
                    }
                    case "payOnline" -> {
                        try {
                            // Attempt to execute the payOnline command
                            userService.payOnline(
                                    command.getEmail(),
                                    command.getCardNumber(),
                                    command.getAmount(),
                                    command.getCurrency(),
                                    command.getTimestamp(),
                                    command.getDescription(),
                                    command.getCommerciant()
                            );
                        } catch (Exception e) {
                            // Add error to the output array only for valid exceptions
                            var errorNode = objectMapper.createObjectNode();
                            errorNode.put("command", "payOnline");

                            // Format the error response
                            var outputNode = errorNode.putObject("output");
                            outputNode.put("timestamp", command.getTimestamp());
                            outputNode.put("description", e.getMessage());

                            errorNode.put("timestamp", command.getTimestamp());
                            output.add(errorNode);
                        }
                    }
                    case "checkCardStatus" -> {
                        try {
                            String status = userService.checkCardStatus(command.getCardNumber(), command.getTimestamp());

                            // Create output for the status check
                            var statusNode = objectMapper.createObjectNode();
                            statusNode.put("command", "checkCardStatus");
                            statusNode.put("timestamp", command.getTimestamp());
                            statusNode.putObject("output").put("status", status);
                            output.add(statusNode);
                        } catch (Exception e) {
                            // Handle error cases
                            var errorNode = objectMapper.createObjectNode();
                            errorNode.put("command", "checkCardStatus");
                            errorNode.putObject("output").put("description", e.getMessage());
                            errorNode.put("timestamp", command.getTimestamp());
                            output.add(errorNode);
                        }
                    }
                    case "sendMoney" -> {
                        try {
                            userService.sendMoney(
                                    command.getAccount(),       // Sender IBAN
                                    command.getAmount(),        // Amount
                                    command.getReceiver(),      // Receiver IBAN
                                    command.getTimestamp(),     // Timestamp
                                    command.getDescription()    // Description
                            );
                        } catch (Exception e) {
                            // Error response
                            var errorNode = objectMapper.createObjectNode();
                            errorNode.put("command", "sendMoney");
                            errorNode.putObject("output").put("description", e.getMessage());
                            errorNode.put("timestamp", command.getTimestamp());
                            output.add(errorNode);
                        }
                    }

                    default -> throw new IllegalArgumentException("Unknown command: " + command.getCommand());
                }
            } catch (Exception e) {
                // Handle errors by adding them to output
                var errorNode = objectMapper.createObjectNode();
                errorNode.put("status", "error");
                errorNode.put("command", command.getCommand());
                errorNode.put("message", e.getMessage());
                output.add(errorNode);
            }
        }

        // Write output to file
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    /**
     * Method used for extracting the test number from the file name.
     *
     * @param file the input file
     * @return the extracted numbers
     */
    public static int fileConsumer(final File file) {
        String fileName = file.getName()
                .replaceAll(CheckerConstants.DIGIT_REGEX, CheckerConstants.EMPTY_STR);
        return Integer.parseInt(fileName.substring(0, 2));
    }
}