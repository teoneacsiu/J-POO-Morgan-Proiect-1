package org.poo.service;

import org.poo.model.ExchangeRate;

import java.util.*;

public class CurrencyExchangeService {
    private final Map<String, List<ExchangeRate>> adjacencyList = new HashMap<>();

    // Add an exchange rate (direct and reverse)
    public void addExchangeRate(String from, String to, double rate) {
        adjacencyList.putIfAbsent(from, new ArrayList<>());
        adjacencyList.putIfAbsent(to, new ArrayList<>());

        adjacencyList.get(from).add(new ExchangeRate(from, to, rate));
        adjacencyList.get(to).add(new ExchangeRate(to, from, 1.0 / rate));
    }

    // Convert an amount from one currency to another
    public double convert(String from, String to, double amount) {
        if (!adjacencyList.containsKey(from) || !adjacencyList.containsKey(to)) {
            throw new IllegalArgumentException("Unknown currency: " + from + " or " + to);
        }
        if (from.equals(to)) {
            return amount; // No conversion needed
        }

        Set<String> visited = new HashSet<>();
        Double result = dfsConvert(from, to, amount, visited);
        if (result == null) {
            throw new IllegalArgumentException("No conversion path between " + from + " and " + to);
        }
        return result;
    }

    // DFS to find conversion ratio
    private Double dfsConvert(String current, String target, double currentAmount, Set<String> visited) {
        visited.add(current);

        // If we reach the target currency
        if (current.equals(target)) {
            return currentAmount;
        }

        // Explore adjacent nodes
        for (ExchangeRate edge : adjacencyList.getOrDefault(current, Collections.emptyList())) {
            if (!visited.contains(edge.getTo())) {
                double convertedAmount = currentAmount * edge.getRate();
                Double result = dfsConvert(edge.getTo(), target, convertedAmount, visited);
                if (result != null) {
                    return result;
                }
            }
        }
        return null; // No path found
    }
}