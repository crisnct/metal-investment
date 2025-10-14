package com.investment.metal.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertDto {

    private long id;

    private String metalSymbol;

    private String expression;

    private String frequency;

}
