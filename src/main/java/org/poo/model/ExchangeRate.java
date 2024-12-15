package org.poo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Reprezintă un curs valutar unidirecțional:
 *  de la o monedă (from) la alta monedă (to) cu rata 'rate'.
 */
@Getter
@Setter
@AllArgsConstructor
public class ExchangeRate {
    private String from;
    private String to;
    private double rate;
}
