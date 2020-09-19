package com.investment.metal.dto;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import lombok.Data;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AppStatusInfoDto {

    private double usdRonRate;

    private String metalPriceHost;

    private CurrencyType metalCurrencyType;

    @Getter
    private Map<MetalType, MetalInfo> metalPrices = new LinkedHashMap<>();

    public void addMetalPrice(MetalType type, MetalInfo info) {
        this.metalPrices.put(type, info);
    }

}
