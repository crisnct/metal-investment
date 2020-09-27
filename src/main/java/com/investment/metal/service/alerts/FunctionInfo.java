package com.investment.metal.service.alerts;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
