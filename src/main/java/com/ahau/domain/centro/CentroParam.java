package com.ahau.domain.centro;

import lombok.Data;

@Data
public class CentroParam {
    private Integer minPeriod;
    private Integer maxPeriod;
    private Integer minLength;
    private Integer maxGap;
    private String prefix;
}
