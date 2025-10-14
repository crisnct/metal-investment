package com.investment.metal.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ExpressionFunctionParameterMinMaxDto extends ExpressionFunctionParameterDto {

    private double min;

    private double max;

}
