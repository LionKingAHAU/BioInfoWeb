package com.ahau.domain.gapFill;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GapStat {
    /*lombok get set方法导致前端JSON属性变小写,*/
    @JsonProperty("cID")
    private String cID;
    private String len;
    @JsonProperty("gCount")
    private String gCount;
    @JsonProperty("gLocus")
    private String gLocus;
}
