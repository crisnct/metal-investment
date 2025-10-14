package com.investment.metal.application.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FunctionInfo {

    private final String name;

    private String description;

    private String returnedType;

    private final List<FunctionParam> parameters = new ArrayList<>();

    public FunctionInfo(String name) {
        this.name = name;
    }

    public void addParam(FunctionParam param) {
        this.parameters.add(param);
    }

}
