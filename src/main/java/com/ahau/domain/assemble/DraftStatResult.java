package com.ahau.domain.assemble;

import lombok.Data;

import java.util.Vector;

@Data
public class DraftStatResult {
    // 表格数据
    private Vector<DraftStat> data;
    // 统计数据
    private String totalSize;
    private String gcContent;
}
