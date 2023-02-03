package com.ahau.domain.assemble;

import lombok.Data;

@Data
public class DraftStat {
    private String assemblyID;
    private String length;
    private String gapCount;
    private String gapLocus;
}
