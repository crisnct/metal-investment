package com.investment.metal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetalInfoDto {

    private final double price1kg;

    private final double price1oz;

    private final double price1ozRON;

    private final double revolutPriceAdjustment;

    private final double revolutPrice1oz;

    private String symbol;

}
