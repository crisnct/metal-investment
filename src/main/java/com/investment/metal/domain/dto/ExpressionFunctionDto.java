package com.investment.metal.domain.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ExpressionFunctionDto {
    private String name;

    private String returnedType;

    private String description;

    private List<ExpressionFunctionParameterDto> parameters = new ArrayList<>();

    public void addParameter(ExpressionFunctionParameterDto param){
        this.parameters.add(param);
    }
}
