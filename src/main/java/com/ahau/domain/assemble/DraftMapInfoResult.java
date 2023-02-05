package com.ahau.domain.assemble;

import lombok.Data;

import java.util.Vector;

@Data
public class DraftMapInfoResult {
    // 表格的数据信息统一使用data字段
    private Vector<DraftMapInfo> data;
    // 两个统计信息
    private String totalMapped;
    private String totalDiscarded;
}
