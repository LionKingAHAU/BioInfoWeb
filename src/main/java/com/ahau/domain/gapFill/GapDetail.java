package com.ahau.domain.gapFill;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GapDetail {
    /*lombok get set方法导致前端JSON属性变小写,*/
    @JsonProperty("sID")
    private String sID;
    @JsonProperty("gIdentify")
    private String gIdentify;
    private String status;
    @JsonProperty("cTigID")
    private String cTigID;
    @JsonProperty("cRange")
    private String cRange;
    @JsonProperty("cLen")
    private String cLen;
    @JsonProperty("cStrand")
    private String cStrand;
    @JsonProperty("cScore")
    private String cScore;
}
