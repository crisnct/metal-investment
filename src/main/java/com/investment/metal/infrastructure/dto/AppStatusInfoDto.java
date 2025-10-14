package com.investment.metal.infrastructure.dto;

import com.investment.metal.application.dto.MetalInfoDto;
import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
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
    private Map<MetalType, MetalInfoDto> metalPrices = new LinkedHashMap<>();

    public void addMetalPrice(MetalType type, MetalInfoDto info) {
        this.metalPrices.put(type, info);
    }

}
