package com.ahau.domain.gapFill;

import lombok.Data;

@Data
public class GapParam {
    private Integer minAlignLength;
    private Integer minAlignIdentity;
    private Integer flankLength;
    private Integer maxFillingLength;
    private String prefix;
}
