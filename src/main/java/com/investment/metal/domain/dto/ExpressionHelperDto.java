package com.investment.metal.domain.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ExpressionHelperDto {

    private List<ExpressionFunctionDto> functions = new ArrayList<>();

    public void addFunction(ExpressionFunctionDto func){
        this.functions.add(func);
    }
}
