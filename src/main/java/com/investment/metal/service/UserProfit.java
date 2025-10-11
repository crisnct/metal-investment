package com.investment.metal.service;

import com.investment.metal.database.Customer;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfit {
    private final Customer user;

    private final double metalAmount;

    private final double originalCost;

    private final double currentCost;

    private final double profit;

}
