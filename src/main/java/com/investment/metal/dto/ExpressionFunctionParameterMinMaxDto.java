package com.investment.metal.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ExpressionFunctionParameterMinMaxDto extends ExpressionFunctionParameterDto {

    private double min;

    private double max;

}
