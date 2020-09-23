package com.investment.metal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetalInfo {

    private final double price1kg;

    private final double price1oz;

    private final double price1ozRON;

    private final double revolutPriceAdjustment;

    private String symbol;

}
