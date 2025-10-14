package com.investment.metal.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertsDto {

    private String username;

    private List<AlertDto> alerts;

}
