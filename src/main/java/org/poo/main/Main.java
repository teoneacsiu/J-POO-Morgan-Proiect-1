package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.command.Command;
import org.poo.command.DeleteCardCommand;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.account.Account;
import org.poo.model.Report;
import org.poo.model.SpendingsReport;
import org.poo.transactions.Transaction;
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
import java.util.Map;
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
            currencyExchangeService.addExchangeRate(exchangeInput.getFrom(),
                    exchangeInput.getTo(), exchangeInput.getRate());
        }

        // Initialize UserService and pass CurrencyExchangeService instance
        UserService userService = new UserService(currencyExchangeService);

        // Create output array
        ArrayNode output = objectMapper.createArrayNode();

        // Add users from input
        for (var userInput : inputData.getUsers()) {
            userService.addUser(new User(userInput.getFirstName(),
                    userInput.getLastName(), userInput.getEmail()));
        }

        // Process commands
        for (var command : inputData.getCommands()) {
            switch (command.getCommand()) {
                case "printUsers" -> {
                    // Create and add the snapshot of users to output
                    var printNode = objectMapper.createObjectNode();
                    printNode.put("command", "printUsers");
                    printNode.set("output", userService.getUsersSnapshot(objectMapper));
                    printNode.put("timestamp", command.getTimestamp());
                    output.add(printNode);
                }
                case "addAccount" -> userService.addAccount(
                        command.getEmail(),
                        command.getCurrency(),
                        command.getAccountType(),
                        command.getInterestRate(),
                        command.getTimestamp()
                );
                case "addFunds" -> userService.addFundsToAccount(
                        command.getAccount(),
                        command.getAmount()
                );
                case "createCard" -> userService.createCardForAccount(
                        command.getEmail(),
                        command.getAccount(),
                        command.getTimestamp()
                );
                case "deleteAccount" -> {
                    try {
                        userService.deleteAccount(
                                command.getEmail(),
                                command.getAccount(),
                                command.getTimestamp()
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
                    } catch (Exception e) {
                        // Add error message to the output
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "deleteAccount");

                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("error",
                                "Account couldn't be deleted - "
                                        + "see org.poo.transactions for details");
                        outputNode.put("timestamp", command.getTimestamp());

                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }

                case "createOneTimeCard" -> userService.createOneTimeCard(
                        command.getEmail(),
                        command.getAccount(),
                        command.getTimestamp());
                case "deleteCard" -> {
                    try {
                        Command deleteCardCommand = new DeleteCardCommand(
                                userService,
                                command.getEmail(),
                                command.getCardNumber(),
                                command.getTimestamp()
                        );
                        deleteCardCommand.execute();
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
                                command.getAmount()  // Minimum balance to set
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
                        userService.payOnline(
                                command.getEmail(),
                                command.getCardNumber(),
                                command.getAmount(),
                                command.getCurrency(),
                                command.getTimestamp(),
                                command.getCommerciant()
                        );
                    } catch (Exception e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "payOnline");
                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("timestamp", command.getTimestamp());
                        outputNode.put("description", e.getMessage());
                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }

                case "checkCardStatus" -> {
                    if (userService.checkCardStatus(command.getCardNumber(), command.getTimestamp())) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "checkCardStatus");
                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("timestamp", command.getTimestamp());
                        outputNode.put("description", "Card not found");
                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }
                case "sendMoney" -> {
                    // Tries to do the transfer without throwing exceptions
                    Account senderAccount =
                            userService.findAccountByAliasOrIBAN(command.getAccount());
                    Account receiverAccount =
                            userService.findAccountByAliasOrIBAN(command.getReceiver());

                    if (senderAccount == null || receiverAccount == null) {
                        // If one of the accounts is not found, we break;
                        break;
                    }

                    userService.sendMoney(
                            command.getAccount(),
                            command.getAmount(),
                            command.getReceiver(),
                            command.getTimestamp(),
                            command.getDescription(),
                            command.getEmail()
                    );
                }

                case "setAlias" -> {
                    try {
                        userService.setAlias(command.getEmail(),
                                command.getAlias(), command.getAccount());
                    } catch (Exception e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("status", "error");
                        errorNode.put("command", "setAlias");
                        errorNode.put("message", e.getMessage());
                        output.add(errorNode);
                    }
                }
                case "printTransactions" -> {
                    try {
                        // Find the user by email
                        User user = userService.findUserByEmail(command.getEmail());
                        user.getTransactions().sort(Comparator.comparing(Transaction::getTimestamp));

                        // Initialize the array of transactions
                        ArrayNode transactionsOutput = objectMapper.createArrayNode();

                        for (Transaction transaction : user.getTransactions()) {
                            // Create a JSON object for the transaction
                            ObjectNode transactionNode;

                            transactionNode = transaction.toJson();

                            transactionsOutput.add(transactionNode);
                        }

                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("command", "printTransactions");
                        outputNode.set("output", transactionsOutput);
                        outputNode.put("timestamp", command.getTimestamp());
                        output.add(outputNode);

                    } catch (Exception e) {
                        // Generate an error message
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "printTransactions");
                        errorNode.putObject("output").put("description", e.getMessage());
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }
                case "addInterest" -> {
                    try {
                        userService.addInterest(command.getAccount());
                    } catch (Exception e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "addInterest");
                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("timestamp", command.getTimestamp());
                        outputNode.put("description", e.getMessage());
                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }
                case "changeInterestRate" -> {
                    try {
                        userService.changeInterestRate(command);
                    } catch (Exception e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "changeInterestRate");
                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("timestamp", command.getTimestamp());
                        outputNode.put("description", e.getMessage());
                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }
                case "splitPayment" -> userService.splitPayment(command);
                case "report" -> {
                    try {
                        Report report = userService.generateReport(command);
                        var reportNode = objectMapper.createObjectNode();
                        reportNode.put("command", "report");

                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("IBAN", command.getAccount());
                        outputNode.put("balance", report.getBalance());
                        outputNode.put("currency", report.getCurrency());

                        var transactionsArray = objectMapper.createArrayNode();
                        for (Transaction transaction : report.getTransactions()) {
                            transactionsArray.add(transaction.toJson());
                        }
                        outputNode.set("transactions", transactionsArray);

                        reportNode.set("output", outputNode);
                        reportNode.put("timestamp", command.getTimestamp());

                        output.add(reportNode);
                    } catch (Exception e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "report");
                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("timestamp", command.getTimestamp());
                        outputNode.put("description", e.getMessage());
                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }
                case "spendingsReport" -> {
                    try {
                        SpendingsReport spendingsReport = userService.generateSpendingsReport(command);
                        spendingsReport.setCommerciants();

                        var reportNode = objectMapper.createObjectNode();
                        reportNode.put("command", "spendingsReport");

                        var outputNode = objectMapper.createObjectNode();
                        outputNode.put("IBAN", command.getAccount());
                        outputNode.put("balance", spendingsReport.getBalance());
                        outputNode.put("currency", spendingsReport.getCurrency());

                        var transactionsArray = objectMapper.createArrayNode();
                        for (Transaction transaction : spendingsReport.getTransactions()) {
                            transactionsArray.add(transaction.toJson());
                        }
                        outputNode.set("transactions", transactionsArray);

                        var commerciantsArray = objectMapper.createArrayNode();

                        spendingsReport.getCommerciants().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(entry -> {
                                    var commerciantNode = objectMapper.createObjectNode();
                                    commerciantNode.put("commerciant", entry.getKey());
                                    commerciantNode.put("total", entry.getValue());
                                    commerciantsArray.add(commerciantNode);
                                });

                        outputNode.set("commerciants", commerciantsArray);

                        reportNode.set("output", outputNode);
                        reportNode.put("timestamp", command.getTimestamp());

                        output.add(reportNode);
                    } catch (Exception e) {
                        var errorNode = objectMapper.createObjectNode();
                        errorNode.put("command", "spendingsReport");
                        var outputNode = objectMapper.createObjectNode();

                        if (e.getMessage().equalsIgnoreCase("account not found")) {
                            outputNode.put("timestamp", command.getTimestamp());
                            outputNode.put("description", e.getMessage());
                        } else {
                            outputNode.put("error", e.getMessage());
                        }

                        errorNode.set("output", outputNode);
                        errorNode.put("timestamp", command.getTimestamp());
                        output.add(errorNode);
                    }
                }
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
