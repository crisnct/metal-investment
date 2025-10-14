package com.investment.metal.application.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public class UserMetalInfoDto {

    @Getter
    private final String metalSymbol;

    @Getter
    private final double amountPurchased;

    @Getter
    private final double costPurchased;

    @Getter
    private final double costNow;

    @Getter
    private final double profit;

}
