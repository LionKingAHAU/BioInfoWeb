package com.ahau.domain.centro;

import com.ahau.domain.ProcessWarning;
import lombok.Data;

import java.util.ArrayList;
import java.util.Vector;

@Data
public class CentroResultUrl {
    private String genomePng;
    private String candidateUrl;
    private String fastaZipUrl;
    private String gff3ZipUrl;
    private String candidateZipUrl;
    private Vector<ProcessWarning> warnings;
    private ArrayList<CentroCandidate> candidate;
}
