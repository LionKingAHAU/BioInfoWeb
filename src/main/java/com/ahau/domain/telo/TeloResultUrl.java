package com.ahau.domain.telo;

import com.ahau.domain.ProcessWarning;
import lombok.Data;

import java.util.ArrayList;
import java.util.Vector;

@Data
public class TeloResultUrl {
    private String genomePng;
    private String infoUrl;
    private ArrayList<TeloInfo> teloInfo;
    private Vector<ProcessWarning> warnings;
}
