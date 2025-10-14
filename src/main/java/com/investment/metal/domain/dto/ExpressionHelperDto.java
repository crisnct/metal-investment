package com.investment.metal.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExpressionHelperDto {

    private List<ExpressionFunctionDto> functions = new ArrayList<>();

    public void addFunction(ExpressionFunctionDto func){
        this.functions.add(func);
    }
}
