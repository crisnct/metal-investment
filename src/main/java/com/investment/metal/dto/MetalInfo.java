package com.investment.metal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetalInfo {

    private String symbol;

    private final double price;

    private final double revolutPriceAdjustment;

}
