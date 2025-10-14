package com.investment.metal.application.dto;

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
    private final List<UserMetalInfoDto> metalInfo = new ArrayList<>();

    public ProfitDto(String username) {
        this.username = username;
        this.time = new Timestamp(System.currentTimeMillis());
    }

    public void addInfo(UserMetalInfoDto info) {
        this.metalInfo.add(info);
    }

}
