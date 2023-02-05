package com.ahau.domain.gapFill;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GapStatResult {
    // 1. 表格数据
    private ArrayList<GapStat> data;
    // 2. 统计信息
    private String totalSize;
    private String gcContent;
}
