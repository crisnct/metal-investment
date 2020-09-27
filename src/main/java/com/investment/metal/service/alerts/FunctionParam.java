package com.investment.metal.service.alerts;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FunctionParam {

    private final String name;

    private final String description;

    private final double min;

    private final double max;

}
