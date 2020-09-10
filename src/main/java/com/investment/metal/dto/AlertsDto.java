package com.investment.metal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AlertsDto {

    private String username;

    private List<AlertDto> alerts;

}
