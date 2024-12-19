package org.poo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a unidirectional exchange rate:
 * from one currency (from) to another currency (to) with a specific 'rate'.
 */
@Getter
@Setter
@AllArgsConstructor
public class ExchangeRate {
    private String from;
    private String to;
    private double rate;
}
