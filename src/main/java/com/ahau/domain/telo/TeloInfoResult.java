package com.ahau.domain.telo;

import lombok.Data;

import java.util.ArrayList;

@Data
public class TeloInfoResult {
    // 1. 表格数据
    private ArrayList<TeloInfo> data;
    // 2. 统计数据
    private String telomereRepeat;
    private String bothFound;
    private String onlyOne;
    private String noFound;
}
