package com.ahau.domain.gapFill;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GapDetailResult {
    // 1. 表格数据
    private ArrayList<GapDetail> data;
    // 2. 统计数据
    private String gapClosed;
    private String totalFilledLength;
    private String gapRemains;
}
