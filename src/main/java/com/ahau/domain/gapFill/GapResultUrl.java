package com.ahau.domain.gapFill;

import com.ahau.domain.ProcessWarning;
import lombok.Data;
import java.util.ArrayList;
import java.util.Vector;


@Data
public class GapResultUrl {
    // 1 genomePng
    private String genomePng;
    // 2 detailed
    private ArrayList<GapDetail> gapDetail;
    // detailUrl
    private String detailUrl;
    // 3 stat
    private ArrayList<GapStat> gapStat;
    private String statUrl;
    // 4 fastaUrl
    private String fastaUrl;
    // 5 Warning info
    private Vector<ProcessWarning> warnings;
}
