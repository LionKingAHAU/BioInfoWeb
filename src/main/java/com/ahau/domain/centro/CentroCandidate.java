package com.ahau.domain.centro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Vector;

@Data
public class CentroCandidate {
    private String chr;
    private String start;
    private String end;
    private String length;
    @JsonProperty("TRLength")
    private String TRLength;
    @JsonProperty("TRCoverage")
    private String TRCoverage;
    @JsonProperty("TELength")
    private String TELength;
    @JsonProperty("TECoverage")
    private String TECoverage;
    private String regionScore;
    private Vector<CentroSubCan> subInfo;
}
