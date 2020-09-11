package com.investment.metal.dto;

import lombok.Getter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProfitDto {

    @Getter
    private final String username;

    @Getter
    private final Timestamp time;

    @Getter
    private final List<MetalInfo> metalInfo = new ArrayList<>();

    public ProfitDto(String username) {
        this.username = username;
        this.time = new Timestamp(System.currentTimeMillis());
    }

    public void addInfo(MetalInfo info) {
        this.metalInfo.add(info);
    }

}
