package com.investment.metal.infrastructure.service;

import com.investment.metal.infrastructure.persistence.entity.Customer;
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
