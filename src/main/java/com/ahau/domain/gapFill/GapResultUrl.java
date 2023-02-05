package com.ahau.domain.gapFill;

import com.ahau.domain.ProcessWarning;
import lombok.Data;
import java.util.ArrayList;
import java.util.Vector;


@Data
public class GapResultUrl {
    // 1 genomePng
    private String genomePng;
    // 2 detailed表格信息
    private GapDetailResult gapDetailResult;
    // detailUrl
    private String detailUrl;
    // 3 stat表格信息
    private GapStatResult gapStatResult;
    private String statUrl;
    // 4 fastaUrl
    private String fastaUrl;
    // 5 Warning info
    private Vector<ProcessWarning> warnings;
}
