package com.ahau.service.impl;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.FilenamePair;
import com.ahau.domain.ProcessWarning;
import com.ahau.domain.assemble.DraftMapInfo;
import com.ahau.domain.assemble.DraftResultUrl;
import com.ahau.domain.assemble.DraftStat;
import com.ahau.domain.centro.CentroCandidate;
import com.ahau.domain.centro.CentroResultUrl;
import com.ahau.domain.centro.CentroSubCan;
import com.ahau.domain.gapFill.GapDetail;
import com.ahau.domain.gapFill.GapResultUrl;
import com.ahau.domain.gapFill.GapStat;
import com.ahau.domain.telo.TeloInfo;
import com.ahau.domain.telo.TeloResultUrl;
import com.ahau.exception.BusinessException;
import com.ahau.exception.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

@Service
public class CommonService {

    // 1. 浏览器访问的地址 （绝对路径）
    @Value("${pathConfig.displayPath}")
    private String displayPath;
    // 2. 后端进行读写的地址（相对路径）
    @Value("${pathConfig.rwRootPath}")
    private String rwRootPath;
    // 3. 上传文件的地址
    @Value("${pathConfig.uploadPath}")
    private String uploadDir;


    /**
     * @Description: 模拟MultiPartFile的transferTo()函数，该函数使用相对路径会自动拼接父路径，所以使用IO流写入
     * @Param: MultiPartFile file
     * @Param: String uploadPath
     * @Return: void
     */
    public void transfer(MultipartFile file, String uploadPath) throws IOException {
        System.out.println("--->CommonService: transfer......");
        InputStream inputStream = file.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(uploadPath);
        byte[] bytes = new byte[1024];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, len);
        }
        inputStream.close();
        fileOutputStream.close();
    }


    /**
     * @Description: 单独文件的上传，只把一个文件转存到指定位置
     * @Param: MultiPartFile file 上传的文件
     * @Return: String 文件名
     */
    public String upload(MultipartFile file) {
        System.out.println("--->CommonService: upload......");
        // 1. 创建UUID 获取原始文件名
        String originName = file.getOriginalFilename();
        String uuidName = UUID.randomUUID().toString() + "_" + originName;
        // 2. 转存文件 transferTo需要使用绝对路径 所以改为IO流
        String uploadPath = uploadDir + uuidName;
        System.out.println("------>上传文件地址：" + uploadPath);
        try {
            transfer(file, uploadPath);
        } catch (IOException exception) {
            throw new BusinessException("error", Code.UPLOAD_ERR);
        }
        return uuidName;
    }


    /**
     * @Description: 单独文件的移除，通过Session的属性名获取文件的uuidName，然后删除
     * @Param: HttpServletRequest request
     * @Param: String paramType
     * @Return: Boolean 返回是否删除成功
     */
    public Boolean remove(HttpServletRequest request, String paramType) {
        System.out.println("--->CommonService：remove......");
        // 1. 从Session获取到属性
        HttpSession session = request.getSession();
        String uuidName = (String) session.getAttribute(paramType);
        // 2. 删除文件
        String deletePath = uploadDir + uuidName;
        System.out.println("------> 删除文件地址：" + deletePath);
        // 删除Session
        session.removeAttribute(paramType);
        // 3. 删除文件
        File deleteFile = new File(deletePath);
        return deleteFile.delete();
    }


    /**
     * @Description: 多个文件的上传，由于是多个文件，需要得知前端文件名和后端唯一文件名的关系，所以使用不同的函数处理
     * @Param: MultipartFile file,
     * @Param: HttpServletRequest request
     * @Param: String paramType
     * @Return: Result
     */
    public Result multiUpload(MultipartFile file, HttpServletRequest request, String paramType) {
        System.out.println("---> commonService：multiUpload......");
        // 0. 转存文件 新建对象
        String uuidName = upload(file);
        FilenamePair filenamePair = new FilenamePair();
        filenamePair.setOriginName(file.getOriginalFilename());
        filenamePair.setUuidName(uuidName);
        // 1. 获取Session的对应属性
        HttpSession session = request.getSession();
        ArrayList<FilenamePair> filenamePairs = (ArrayList<FilenamePair>) session.getAttribute(paramType);
        // 2. 若属性为空 新建数组
        if (filenamePairs == null) {
            System.out.println("------->首次添加文件");
            filenamePairs = new ArrayList<FilenamePair>();
        }
        // 3. 添加对象
        filenamePairs.add(filenamePair);
        // 4. 设置Session
        session.setAttribute(paramType, filenamePairs);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: 多个文件的删除，主要是GapFill的模块使用，通过前端给出删除文件的原始名，删除存储的唯一名
     * @Param: HttpServletRequest request
     * @Param: String filename
     * @Param: String paramType
     * @Return: Boolean 是否删除成功
     */
    public Boolean multiRemove(HttpServletRequest request, String filename, String paramType) {
        System.out.println("--->CommonService：multiRemove......");
        Boolean delete = null;
        // 1. 获取Session
        HttpSession session = request.getSession();
        // 2. 得到属性
        ArrayList<FilenamePair> filenamePairs = (ArrayList<FilenamePair>) session.getAttribute(paramType);
        // 3. 获取所有文件对象 遍历 若原始文件名相同
        for (FilenamePair filenamePair : filenamePairs) {
            if (filenamePair.getOriginName().equals(filename)) {
                // 获取对应的uuid名
                String deletePath = uploadDir + filenamePair.getUuidName();
                System.out.println("------> 删除文件的路径为" + deletePath);
                // 删除文件
                File deleteFile = new File(deletePath);
                delete = deleteFile.delete();
                // 删除Session中对应属性
                filenamePairs.remove(filenamePair);
            }
        }
        // 4. 重新设置Session
        session.setAttribute(paramType, filenamePairs);
        return delete;
    }


    /**
     * @Description: AssembleDisplay 根据训练的结果给Session设置值
     * @Param: request
     * @Return: ResultUrl
     */
    public DraftResultUrl assembleDisplay(HttpServletRequest request) {
        System.out.println("---> commonService: assembleDisplay......");
        // 初始化DraftResultUrl
        DraftResultUrl draftResultUrl = new DraftResultUrl();
        // 从Session中获取TaskID Warnings等信息
        HttpSession session = request.getSession();
        String taskID = (String) session.getAttribute("TaskID"); // Assemble/uuid/
        Vector<ProcessWarning> warningInfos = (Vector<ProcessWarning>) session.getAttribute("assembleWarnings");
        draftResultUrl.setWarnings(warningInfos);
        // 1. 绝对路径和相对路径
        String abPath = displayPath + taskID;
        String rlPath = rwRootPath + taskID;
        System.out.println("------>adPath：\t" + abPath);
        System.out.println("------>rlPath：\t" + rlPath);
        // 2. 对于taskID目录是否存在 + 目录中是否有结果
        File rlDir = new File(rlPath);
        File[] genFiles = rlDir.listFiles();
        // 3. 若检索条目不存在 或 目录为空，说明运行中发生错误，导致没有初始化任务目录，脚本运行没有结果
        if (!rlDir.exists() || genFiles == null) {
            return null;
        } else {
            // 遍历文件目录
            for (File file : genFiles) {
                // 对文件名后缀进行筛选，给DraftResultUrl设置属性
                String fileName = file.getName(); // 文件名
                String fileDisplay = abPath + fileName; // 文件存储的绝对路径
                String fileRead = rlPath + fileName; // 文件存储的相对路径
                // ContigMapRefPdf
                if (fileName.contains("contig_map_ref")) {
                    draftResultUrl.setHifiRefUrl(fileDisplay);
                }
                // 只有选择Plot才有的 GenomeMapRefPdf
                if (fileName.contains("genome_map_ref")) {
                    draftResultUrl.setGenomeRefUrl(fileDisplay);
                }
                // png图
                if (fileName.contains("genome.png")) {
                    draftResultUrl.setGenomePng(fileDisplay);
                }
                // MapInfo表格链接和读
                if (fileName.contains("mapinfo")) {
                    draftResultUrl.setMapInfoUrl(fileDisplay);
                    // 读表格
                    try {
                        Vector<DraftMapInfo> draftMapInfos = draftReadMapInfo(fileRead);
                        draftResultUrl.setDraftMapInfo(draftMapInfos);
                    } catch (IOException e) {
                        throw new SystemException("ReadFailed！", Code.SYSTEM_ERR);
                    }
                }
                // Stat表格链接和读
                if (fileName.contains("stat")) {
                    draftResultUrl.setStatUrl(fileDisplay);
                    // 读表格
                    try {
                        Vector<DraftStat> draftStats = draftReadStat(fileRead);
                        draftResultUrl.setDraftStat(draftStats);
                    } catch (IOException e) {
                        throw new SystemException("ReadFailed！", Code.SYSTEM_ERR);
                    }
                }
                // 下载的Fasta链接
                if (fileName.contains("fasta")) {
                    draftResultUrl.setFastaUrl(fileDisplay);
                }
                // 下载的AGP链接
                if (fileName.contains("agp")) {
                    draftResultUrl.setAgpUrl(fileDisplay);
                }
                // TODO warning logs
                /*if(fileName.contains("warning")){
                    // 读warnings
                    draftResultUrl.setWarnings();
                }*/
            }
        }
        System.out.println(draftResultUrl);
        return draftResultUrl;
    }


    /**
     * @Description: 读取mapInfo文件 然后返回一个对应格式的数组
     * @Param: String mapInfoUrl
     * @Return: Vector<DraftMapInfo>
     */
    public Vector<DraftMapInfo> draftReadMapInfo(String mapInfoUrl) throws IOException {
        // 读取得从本地读取 这里要由绝对路径转为相对路径
        System.out.println("---> commonService：draftReadMapInfo......");
        System.out.println("------>mapInfoUrl:" + mapInfoUrl);
        Vector<DraftMapInfo> draftMapInfos = new Vector<>();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(mapInfoUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                DraftMapInfo draftMapInfo = new DraftMapInfo();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            draftMapInfo.setContigID(split[i]);
                            break;
                        case 1:
                            draftMapInfo.setContigLength(split[i]);
                            break;
                        case 2:
                            draftMapInfo.setTargetID(split[i]);
                            break;
                    }
                }
                draftMapInfos.add(draftMapInfo);
            }
            line = reader.readLine();
        }
        reader.close();
       /* for (DraftMapInfo draftMapInfo : draftMapInfos) {
            System.out.println(draftMapInfo);
        }*/
        return draftMapInfos;
    }


    /**
     * @Description: 读取stat文件
     * @Param: String statUrl
     * @Return: Vector<DraftStat>
     */
    public Vector<DraftStat> draftReadStat(String statUrl) throws IOException {
        System.out.println("---> commonService：draftReadStat......");
        Vector<DraftStat> draftStats = new Vector<>();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(statUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                DraftStat draftStat = new DraftStat();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            draftStat.setAssemblyID(split[i]);
                            break;
                        case 1:
                            draftStat.setLength(split[i]);
                            break;
                        case 2:
                            draftStat.setGapCount(split[i]);
                            break;
                        case 3: // 该列可能有多个
                            StringBuilder gLocus = new StringBuilder();
                            for (int j = i; j < split.length; j++) {
                                gLocus.append(split[j]).append("\t");
                            }
                            draftStat.setGapLocus(gLocus.toString());
                            break;
                    }
                }
                draftStats.add(draftStat);
            }
            line = reader.readLine();
        }
        reader.close();
        /*for (DraftStat draftStat : draftStats) {
            System.out.println(draftStat);
        }*/
        return draftStats;
    }


    /**
     * @Description: gapFill的Display
     * @Param: HttpServletRequest request
     * @Return: GapResultUrl
     */

    public GapResultUrl gapFillDisplay(HttpServletRequest request) {
        System.out.println("--->commonService: FillDisplay......");
        // 初始化DraftResultUrl
        GapResultUrl gapResultUrl = new GapResultUrl();
        // 从Session中获取TaskID Warnings等信息
        HttpSession session = request.getSession();
        String taskID = (String) session.getAttribute("TaskID"); // GapFill/uuid/
        Vector<ProcessWarning> warningInfos = (Vector<ProcessWarning>) session.getAttribute("fillWarnings");
        gapResultUrl.setWarnings(warningInfos);
        // 1. 绝对路径和相对路径
        String abPath = displayPath + taskID;
        String rlPath = rwRootPath + taskID;
        System.out.println("------>adPath：\t" + abPath);
        System.out.println("------>rlPath：\t" + rlPath);
        // 2. 对于taskID目录是否存在 + 目录中是否有结果
        File rlDir = new File(rlPath);
        File[] genFiles = rlDir.listFiles();
        // 3. 若检索条目不存在 或 目录为空，说明运行中发生错误，导致没有初始化任务目录，脚本运行没有结果
        if (!rlDir.exists() || genFiles == null) {
            return null;
        } else {
            // 遍历文件目录
            for (File file : genFiles) {
                // 对文件名后缀进行筛选，给DraftResultUrl设置属性
                String fileName = file.getName(); // 文件名
                String fileDisplay = abPath + fileName; // 文件存储的绝对路径
                String fileRead = rlPath + fileName; // 文件存储的相对路径
                // genomePng
                if (fileName.contains("png")) {
                    gapResultUrl.setGenomePng(fileDisplay);
                }
                // fasta
                if (fileName.contains("fasta")) {
                    gapResultUrl.setFastaUrl(fileDisplay);
                }
                // stat表格链接和读
                if (fileName.contains("stat")) {
                    gapResultUrl.setStatUrl(fileDisplay);
                    // 读表格
                    try {
                        ArrayList<GapStat> gapStats = fillReadStat(fileRead);
                        gapResultUrl.setGapStat(gapStats);
                    } catch (IOException e) {
                        throw new SystemException("ReadFailed！", Code.SYSTEM_ERR);
                    }
                }
                // detail表格链接和读
                if (fileName.contains("detail")) {
                    gapResultUrl.setDetailUrl(fileDisplay);
                    // 读表格
                    try {
                        ArrayList<GapDetail> gapDetails = fillReadDetail(fileRead);
                        gapResultUrl.setGapDetail(gapDetails);
                    } catch (IOException e) {
                        throw new SystemException("ReadFailed！", Code.SYSTEM_ERR);
                    }
                }
            }
        }
        System.out.println(gapResultUrl);
        return gapResultUrl;
    }


    /**
     * @Description: fillReadStat 读取fill module的stat数据并回显到页面中
     * @Param: StatUrl
     * @Return: gapStat
     */
    public ArrayList<GapStat> fillReadStat(String statUrl) throws IOException {
        System.out.println("--->CommonService：fillReadStat 读取stat内容到前端......");
        System.out.println("------>mapInfoUrl:" + statUrl);
        ArrayList<GapStat> gapStats = new ArrayList<>();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(statUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                GapStat gapStat = new GapStat();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            gapStat.setCID(split[i]);
                            break;
                        case 1:
                            gapStat.setLen(split[i]);
                            break;
                        case 2:
                            gapStat.setGCount(split[i]);
                            break;
                        case 3:
                            // 该项可能有多个(希望源文件可以使用和\t不同的分隔符)
                            StringBuilder gLocus = new StringBuilder();
                            for (int j = i; j < split.length; j++) {
                                gLocus.append(split[j]).append("\t");
                            }
                            gapStat.setGLocus(gLocus.toString());
                            break;
                    }
                }
                gapStats.add(gapStat);
            }
            line = reader.readLine();
        }
        reader.close();
       /* for (GapStat gapStat : gapStats) {
            System.out.println(gapStat);
        }*/
        return gapStats;
    }


    /**
     * @Description: fillReadDetail 读取fill模块的detail数据并返回到页面中
     * @Param: DetailUrl
     * @Return: gapDetails
     */
    public ArrayList<GapDetail> fillReadDetail(String detailUrl) throws IOException {
        System.out.println("--->CommonService：fillReadDetail 读取detail内容到前端......");
        ArrayList<GapDetail> gapDetails = new ArrayList<>();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(detailUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                GapDetail gapDetail = new GapDetail();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            gapDetail.setSID(split[i]);
                            break;
                        case 1:
                            gapDetail.setGIdentify(split[i]);
                            break;
                        case 2:
                            gapDetail.setStatus(split[i]);
                            break;
                        case 3:
                            gapDetail.setCTigID(split[i]);
                            break;
                        case 4:
                            gapDetail.setCRange(split[i]);
                            break;
                        case 5:
                            gapDetail.setCLen(split[i]);
                            break;
                        case 6:
                            gapDetail.setCStrand(split[i]);
                            break;
                        case 7:
                            gapDetail.setCScore(split[i]);
                            break;
                    }
                }
                gapDetails.add(gapDetail);
            }
            line = reader.readLine();
        }
        reader.close();
        /*for (GapDetail gapDetail : gapDetails) {
            System.out.println(gapDetail);
        }*/
        return gapDetails;
    }


    /**
     * @Description: telo模块的展示
     * @Param: HttpServletRequest request
     * @Return: TeloResultUrl
     */
    public TeloResultUrl teloDisplay(HttpServletRequest request) {
        System.out.println("--->commonService: teloDisplay......");
        // 初始化TeloResultUrl
        TeloResultUrl teloResultUrl = new TeloResultUrl();
        // 从Session中获取TaskID Warnings等信息
        HttpSession session = request.getSession();
        String taskID = (String) session.getAttribute("TaskID"); // taskID: Telo/uuid/
        Vector<ProcessWarning> warnings = (Vector<ProcessWarning>) session.getAttribute("teloWarnings");
        teloResultUrl.setWarnings(warnings);
        // 1. 绝对路径和相对路径
        String abPath = displayPath + taskID;
        String rlPath = rwRootPath + taskID;
        System.out.println("------>adPath：\t" + abPath);
        System.out.println("------>rlPath：\t" + rlPath);
        // 2. 对于taskID目录是否存在 + 目录中是否有结果
        File rlDir = new File(rlPath);
        File[] genFiles = rlDir.listFiles();
        // 3. 若检索条目不存在 或 目录为空，说明运行中发生错误，导致没有初始化任务目录，脚本运行没有结果
        if (!rlDir.exists() || genFiles == null) {
            return null;
        } else {
            // 遍历文件目录
            for (File file : genFiles) {
                // 对文件名后缀进行筛选，给DraftResultUrl设置属性
                String fileName = file.getName(); // 文件名
                String fileDisplay = abPath + fileName; // 文件存储的绝对路径
                String fileRead = rlPath + fileName; // 文件存储的相对路径
                // png
                if (fileName.contains("png")) {
                    teloResultUrl.setGenomePng(fileDisplay);
                }
                // info表格链接和读取
                if (fileName.contains("info")) {
                    teloResultUrl.setInfoUrl(fileDisplay);
                    // 读表格
                    try {
                        ArrayList<TeloInfo> teloInfos = teloReadInfo(fileRead);
                        teloResultUrl.setTeloInfo(teloInfos);
                    } catch (IOException e) {
                        throw new SystemException("ReadFailed！", Code.SYSTEM_ERR);
                    }
                }
            }
        }
        System.out.println(teloResultUrl);
        return teloResultUrl;
    }

    /**
     * @Description: teloInfo读取回到主页
     * @Param: String infoUrl
     * @Return: ArrayList<TeloInfo>
     */
    public ArrayList<TeloInfo> teloReadInfo(String infoUrl) throws IOException {
        System.out.println("--->CommonService：teloReadDetail 读取info内容到前端......");
        ArrayList<TeloInfo> teloInfos = new ArrayList<>();
        BufferedReader reader;
        System.out.println(infoUrl);
        reader = new BufferedReader(new FileReader(infoUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                TeloInfo teloInfo = new TeloInfo();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            teloInfo.setChrID(split[i]);
                            break;
                        case 1:
                            teloInfo.setChrLength(split[i]);
                            break;
                        case 2:
                            teloInfo.setStatus(split[i]);
                            break;
                        case 3:
                            teloInfo.setLeftNum(split[i]);
                            break;
                        case 4:
                            teloInfo.setLeftDirection(split[i]);
                            break;
                        case 5:
                            teloInfo.setRightNum(split[i]);
                            break;
                        case 6:
                            teloInfo.setRightDirection(split[i]);
                            break;
                    }
                }
                teloInfos.add(teloInfo);
            }
            line = reader.readLine();
        }
        reader.close();
       /* for (TeloInfo teloInfo : teloInfos) {
            System.out.println(teloInfo);
        }*/
        return teloInfos;
    }


    /**
     * @Description: Centro模块的展示
     * @Param: HttpServletRequest request
     * @Return: CentroResultUrl
     */

    public CentroResultUrl centroDisplay(HttpServletRequest request) {
        System.out.println("--->commonService: CentroDisplay......");
        // 初始化centroResultUrl
        CentroResultUrl centroResultUrl = new CentroResultUrl();
        // 从Session中获取TaskID Warnings等信息
        HttpSession session = request.getSession();
        String taskID = (String) session.getAttribute("TaskID"); // Centro/uuid/
        Vector<ProcessWarning> warningInfos = (Vector<ProcessWarning>) session.getAttribute("centroWarnings");
        centroResultUrl.setWarnings(warningInfos);
        // 1. 绝对路径和相对路径
        String abPath = displayPath + taskID;
        String rlPath = rwRootPath + taskID;
        System.out.println("------>adPath：\t" + abPath);
        System.out.println("------>rlPath：\t" + rlPath);
        // 2. 对于taskID目录是否存在 + 目录中是否有结果
        File rlDir = new File(rlPath);
        File[] genFiles = rlDir.listFiles();
        // 3. 若检索条目不存在 或 目录为空，说明运行中发生错误，导致没有初始化任务目录，脚本运行没有结果
        if (!rlDir.exists() || genFiles == null) {
            return null;
        } else {
            // 遍历文件目录
            for (File file : genFiles) {
                // 对文件名后缀进行筛选，给DraftResultUrl设置属性
                String fileName = file.getName(); // 文件名
                String fileDisplay = abPath + fileName; // 文件存储的绝对路径
                String fileRead = rlPath + fileName; // 文件存储的相对路径
                // CentroPng
                if (fileName.contains("png")) {
                    centroResultUrl.setGenomePng(fileDisplay);
                }
                // best.candidate 读表格和链接
                if (fileName.contains("best.candidate")) {
                    centroResultUrl.setCandidateUrl(fileDisplay);
                    // 读表格
                    try {
                        ArrayList<CentroCandidate> centroCandidates = centroReadCandidate(fileRead);
                        centroResultUrl.setCandidate(centroCandidates);
                    } catch (IOException e) {
                        throw new SystemException("ReadFailed！", Code.SYSTEM_ERR);
                    }
                }
                // candidate.zip
                if (fileName.contains("candidate.zip")) {
                    centroResultUrl.setCandidateZipUrl(fileDisplay);
                }
                // TRgff3
                if (fileName.contains("TRgff3")) {
                    centroResultUrl.setGff3ZipUrl(fileDisplay);
                }
                // TRfasta
                if (fileName.contains("TRfasta")) {
                    centroResultUrl.setFastaZipUrl(fileDisplay);
                }
            }
        }
        System.out.println(centroResultUrl);
        return centroResultUrl;
    }


    /**
     * @Description: centro的bestCandidate读取回到主页
     * @Param: String candidateUrl
     * @Return: ArrayList<CentroCandidate>
     */
    public ArrayList<CentroCandidate> centroReadCandidate(String candidateUrl) throws IOException {
        System.out.println("--->CommonService：centroReadCandidate 读取candidate内容到前端......");
        ArrayList<CentroCandidate> centroCandidates = new ArrayList<>();
        BufferedReader reader;
        System.out.println("------> candidateUrl : " + candidateUrl);
        reader = new BufferedReader(new FileReader(candidateUrl));
        String line = reader.readLine();
        // 如果没有读到结尾
        while (line != null) {
            // 开头的#信息略过
            if (line.startsWith("#")) {
                line = reader.readLine();
            } else if (!line.startsWith("\t")) {
                // 创建主行对象 设置各个属性
                CentroCandidate centroCandidate = new CentroCandidate();
                System.out.println("mainLine---》" + line);
                String[] split = line.split("\t");
                // 设置主属性
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            centroCandidate.setChr(split[i]);
                            break;
                        case 1:
                            centroCandidate.setStart(split[i]);
                            break;
                        case 2:
                            centroCandidate.setEnd(split[i]);
                            break;
                        case 3:
                            centroCandidate.setLength(split[i]);
                            break;
                        case 4:
                            centroCandidate.setTRLength(split[i]);
                            break;
                        case 5:
                            centroCandidate.setTRCoverage(split[i]);
                            break;
                        case 6:
                            centroCandidate.setTELength(split[i]);
                            break;
                        case 7:
                            centroCandidate.setTECoverage(split[i]);
                            break;
                        case 8:
                            centroCandidate.setRegionScore(split[i]);
                            break;
                    }
                }
                // 构建一个子行数组Vector，存放子行对象
                Vector<CentroSubCan> centroSubCans = new Vector<>();
                // 这里开始往下继续读
                line = reader.readLine();
                while ((line != null) && line.startsWith("\t")) {
                    System.out.println("SubLine---》" + line);
                    // 对于每一行，创建子行对象
                    CentroSubCan centroSubCan = new CentroSubCan();
                    String[] splitSub = line.split("\t");
                    // 设置子属性
                    for (int j = 0; j < splitSub.length; j++) {
                        switch (j) {
                            case 1:
                                centroSubCan.setSubTR(splitSub[j]);
                                break;
                            case 2:
                                centroSubCan.setPeriod(splitSub[j]);
                                break;
                            case 3:
                                centroSubCan.setSubTRLength(splitSub[j]);
                                break;
                            case 4:
                                centroSubCan.setSubTRCoverage(splitSub[j]);
                                break;
                            case 5:
                                centroSubCan.setPattern(splitSub[j]);
                                break;
                        }
                    }
                    // 把对象加到数组中
                    centroSubCans.add(centroSubCan);
                    // 主要是这个往下读的动作已经做了，所以不用再read了，不然就会错过本行
                    line = reader.readLine();
                }
                // 循环结束，下一行是主行 这里的line有两种可能，一种是读完了，一种是主行 但是没有利用
                // 把子行数组作为subInfo设置给主行对象
                centroCandidate.setSubInfo(centroSubCans);
                // 主行对象设置完毕，存放到主行数组中
                centroCandidates.add(centroCandidate);
            }
        }
        reader.close();
        // 输出一下看看是否读的正确
       /* for (CentroCandidate centroCandidate : centroCandidates) {
            System.out.println(centroCandidate);
        }*/
        return centroCandidates;
    }


    /**
     * @Description: centro的Show More读Candidate到页面，整体是 uuid/candidate/prefix.chrid.candidate
     * @Param: String fileName: chrID.candiate
     * @Param: HttpServletRequest request
     * @Return: CentroResultUrl
     */
    public ArrayList<CentroCandidate> centroShowMore(HttpServletRequest request, String fileName) {  // fileName: chrID.candiate
        System.out.println("---commonService:centroShowMore......");
        // 1. 获取存储路径
        HttpSession session = request.getSession();
        String taskID = (String) session.getAttribute("TaskID"); // Centro/uuid/
        String candidatePath = rwRootPath + taskID + "candidate/"; // ../../bioRepository/user_dir/Centro/uuid/candidate/
        File candidateFile = new File(candidatePath);
        File[] files = candidateFile.listFiles();
        if (!candidateFile.exists() || files == null) {
            return null;
        } else {
            // 2. 获取Prefix  prefix.chrid.candidate
            String prefix = files[0].getName();
            prefix = prefix.substring(0, prefix.lastIndexOf("."));  // prefix.chrID
            prefix = prefix.substring(0, prefix.lastIndexOf(".") + 1); // prefix.
            String showMorePath = candidatePath + prefix + fileName;
            System.out.println("------>showMorePath:\t");

            // 3. 读表格
            ArrayList<CentroCandidate> centroCandidates = new ArrayList<>();
            try {
                centroCandidates = centroReadCandidate(showMorePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return centroCandidates;
        }
    }
}
