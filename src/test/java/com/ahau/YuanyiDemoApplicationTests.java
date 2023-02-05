package com.ahau;

import com.ahau.common.Code;
import com.ahau.domain.assemble.DraftMapInfo;
import com.ahau.domain.assemble.DraftMapInfoResult;
import com.ahau.domain.assemble.DraftStat;
import com.ahau.domain.centro.CentroCandidate;
import com.ahau.domain.centro.CentroSubCan;
import com.ahau.domain.gapFill.GapDetail;
import com.ahau.domain.gapFill.GapStat;
import com.ahau.domain.telo.TeloInfo;
import com.ahau.exception.BusinessException;
import com.ahau.exception.UserAlreadyExistsException;
import com.ahau.exception.UserNotExistsException;
import com.ahau.exception.WrongPasswordException;
import com.ahau.service.UserService;
import com.ahau.utils.JwtUtil;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
class YuanyiDemoApplicationTests {

    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = simpleDateFormat.format(date);

        System.out.println(sDate);

    }

    @Test
    void UserServiceRegisterTest() {
        try {
            boolean ans = userService.register("123456@qq.com", "123456");
            log.info("注册结果：" + ans);
        } catch (UserAlreadyExistsException e) {
            log.info("注册失败，用户已存在");
        }
    }

    @Test
    void UserLoginAndTokenTest() {
        String email = "123456@qq.com";
        String password = "123456";
        try {
            String token = userService.login(email, password);
            if (token != null && !"".equals(token)) {
                log.info("登录成功，获取到的 token: " + token);
                Map<String, Claim> ans = JwtUtil.verify(token);
                if (ans != null) log.info("验证成功");
                else log.info("验证失败");
                Thread.sleep(11 * 1000L); // 等待 Token 过期
                ans = JwtUtil.verify(token);
                if (ans != null) log.info("验证成功");
                else log.info("验证失败");
            } else {
                log.info("登录失败");
            }
        } catch (UserNotExistsException e) {
            log.info("用户不存在，请先注册");
        } catch (WrongPasswordException e) {
            log.info("密码错误");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 读取stat
    @Test
    void draftReadMapInfo() throws IOException {
        Vector<DraftMapInfo> draftMapInfos = new Vector<>();
        BufferedReader reader;
        /*E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.contig.mapinfo*/
        /*E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.genome.filled.stat*/


        try {
            reader = new BufferedReader(new FileReader(
                    "E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.genome.filled.stat"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println("========" + line + "========");
                String[] tempLine;
                if (!line.contains("#")) {
                    // TODO 这也不是个办法 万一有缺失的值怎么办？字符串是最下等的处理方法了
                    // 万一一个单元格里面有多个数据怎么办？
                    tempLine = line.split("\t");
                    DraftMapInfo dmi = new DraftMapInfo();
                    dmi.setContigID(tempLine[0]);
                    dmi.setContigLength(tempLine[1]);
                    dmi.setTargetID(tempLine[2]);
                    for (String item : tempLine) {
                        System.out.println("--->" + item);
                    }
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void splitTest() {
        String s = "Chr08_Qd\t26490480\t1\t89384-89483";
        String substring = s.substring(s.indexOf("\t"));
        System.out.println(substring);
    }

    @Test
    void draftReadStat() throws IOException {
        Vector<DraftStat> draftStats = new Vector<>();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(
                "E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.draftgenome.stat"));
        String line = reader.readLine();
        while (line != null) {
            System.out.println("========" + line + "========");
//                String[] tempLine;
            if (!line.contains("#")) {
                DraftStat draftStat = new DraftStat();
                // Chr01_Qd\t21163354\t0\t
                // AssemblyID
                int aID = line.indexOf("\t");
                System.out.println("----》" + line.substring(0, aID));
                draftStat.setAssemblyID(line.substring(0, aID));
                // Length
                int len = line.indexOf("\t", aID + 1);
                System.out.println("----》" + line.substring(aID + 1, len));
                draftStat.setLength(line.substring(aID + 1, len));
                // GapCount
                int gCount = line.indexOf("\t", len + 1);
                System.out.println("----》" + line.substring(len + 1, gCount));
                draftStat.setGapCount(line.substring(len + 1, gCount));
                // 然后这后面就是没有的or1个or多个
                if (gCount + 1 == line.length()) {
                    // 说明没有是空的
                    System.out.println("----》 -");
                    draftStat.setGapLocus("-");
                } else {
                    // 说明有一个或多个，通通都是
                    draftStat.setGapLocus(line.substring(gCount + 1));
                    System.out.println("----》" + line.substring(gCount + 1));
                }
                draftStats.add(draftStat);
            }
            line = reader.readLine();
        }
        reader.close();
    }

    @Test
    void fillReadDetail() throws IOException {
        String detailUrl = "QuartetQuartet.genome.filled.detail";
        String transPath = "E:/bioResp/result/genResult/";
        ArrayList<GapDetail> gapDetails = new ArrayList<>();
        BufferedReader reader;
        detailUrl = transPath + detailUrl;
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
        for (GapDetail gapDetail : gapDetails) {
            System.out.println(gapDetail);
        }
    }

    @Test
    void fillReadStat() throws IOException {
        String statUrl = "QuartetQuartet.genome.filled.stat";
        String transPath = "E:/bioResp/result/genResult/";
        ArrayList<GapStat> gapStats = new ArrayList<>();
        BufferedReader reader;
        statUrl = transPath + statUrl;
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
                            // 可能有多个
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
        for (GapStat gapStat : gapStats) {
            System.out.println(gapStat);
        }
    }




    @Test
    void assembleReadMapInfo() throws IOException {
        String mapInfoUrl = "E:/bioResp/result/epResult/Draft/contig.mapinfo";
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
        for (DraftMapInfo draftMapInfo : draftMapInfos) {
            System.out.println(draftMapInfo);
        }
    }

    @Test
    void mkdir() {
        String dir = "E:/bioResp/new task dir/asiajss/upload/1";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("建立任务上传地址目录：" + file);
        }
    }

    @Test
    void delete() {
        String taskID = "Assemble/3a2833bc-d07a-4a32-bcb5-0fa4485ed877";
        String filename = "Genome.fasta";
        String taskRootPath = "E:/bioResp/new_task_dir/";
        String uploadDir = "/upload/";
        String path = taskRootPath + taskID + uploadDir + filename;
        File file = new File(path);
        System.out.println(file.delete());
    }

    @Test
    void getContent() throws IOException {
        File f = new File("E:/bioResp/new_task_dir/Assemble/ef30a6e5-0470-42fe-bc1f-270dca107681/upload");
        // 首先获取该路径下所有目录或文件
        File[] files = f.listFiles();
        // 遍历，若是文件，继续getContent，否则输出绝对路径
        if (files != null) {
            System.out.println(Arrays.toString(files));
        }
    }

    @Test
    void webRead() throws IOException {
        String mapInfoUrl = "http://127.0.0.1:8887/user_dir/Assmble/0f1c4840-8335-4602-9d07-825c9acd787f/result/contig.mapinfo";
//        String mapInfoUrl = "../../bioRepository/user_dir/Assmble/0f1c4840-8335-4602-9d07-825c9acd787f/result/contig.mapinfo";
        System.out.println("----> commonService：draftReadMapInfo");
        System.out.println("mapInfoUrl:" + mapInfoUrl);
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
        for (DraftMapInfo draftMapInfo : draftMapInfos) {
            System.out.println(draftMapInfo);
        }

    }


    // 测试 🆗！：
    // 1. 测试脚本相对路径 2. 上传文件相对脚本的路径
    // 如果能成功运行脚本，并且复制上传的文件到指定目录，则说明正确
    @Test
    void trainTest() {
        String taskID = "Assemble/0f1c4840-8335-4602-9d07-825c9acd787f";
        String assembleGenome = "../../bioRepository/user_dir/" + taskID + "/upload/" + "genome.fasta";
        String hifiGenome = "../../bioRepository/user_dir/" + taskID + "/upload/" + "hifi.fasta";
        // 脚本位置是相对项目来说的
        String exePath = "../../bioRepository/simulatedExec/test.py";
        String cmd = "python " + exePath + " " +
                "-r=" + assembleGenome + " " +
                "-q=" + hifiGenome;

        File testFile = new File(exePath);
        System.out.println(testFile.exists());
        File aFile = new File(assembleGenome);
        System.out.println(aFile.exists());
        File hFile = new File(hifiGenome);
        System.out.println(hFile.exists());

        System.out.println(cmd);

        System.out.println("=========TrainService -> train 通用调用进程执行命令===========");
        // 1. 创建进程对象
        Process process;
        // 2. 存储命令行打印的读取结果
        Vector<String> execResult = new Vector<>();
        try {
            // 3. 使用Runtime.getRuntime()创建一个本地进程
            process = Runtime.getRuntime().exec(cmd);
            // 5. 定义脚本的输出
            String result = null;
            // 6. cmd返回流 BufferedInputStream：字节缓冲流， 需要提供process返回连接到子进程正常输出的输入流
            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            // 7. 字符流转换字节流 BufferedReader：从字符输入流中读取文本，缓冲字符； InputStreamReader:从字节流到字符流的桥梁
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // 【注意】如果你要输出中文字符，在这里需要给字符输入流加一个指定charset字符集，我这里把注释掉了，你可以自己选择
            //  BufferedReader br1 = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            // 8. 进行读取和输出
            String lineStr = null;
            while ((lineStr = br.readLine()) != null) {
                result = lineStr;
                execResult.add(lineStr);
            }
            // 关闭输入流
            br.close();
            in.close();
            // 4. 如有必要，使当前线程等待，直到此Process对象表示的进程终止。
            process.waitFor();
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.TRAIN_ERR);
        }
        // 9. 输出这个String Vector
        System.out.println("------》 打印cmd Result结果地址：");
        for (String s : execResult) {
            System.out.println(s);
        }

    }

    @Test
    void subString() {
        String path = "../../bioRepository/user_dir/";
        System.out.println(path.substring(6));
        System.out.println(path.indexOf("bioRepository"));
    }

    @Test
    void abc() {
        File file = new File("../../bioRepository/user_dir/upload/");
        System.out.println(file.exists());
    }

    // 测试，在指定的目录运行命令
    @Test
    void runInDir() throws IOException, InterruptedException {
        Process process;
        // 1. 这是相对执行目录来说的
        // 相对脚本存储目录
        String exePath = "../../../simulatedExec/test.py";
        // a文件目录
        String aFile = "../../upload/4fb38236-44d8-4065-8b08-532ab677bd5f_HiFi.fasta";
        String bFile = "../../upload/8d179bb6-9a90-4f0f-8c21-d00f4bd49879_Genome.fasta";
        String prefix = "myfile";
        // 拼接指令
        String cmd = "python" + " " +
                exePath + " " +
                "-a=" + aFile + " " +
                "-b=" + bFile + " " +
                "-p=" + prefix;
        System.out.println("cmd:\t" + cmd);
        // 2. 执行目录
        String pathname = "../../bioRepository/user_dir/Assemble/simulateUUID";
        File file = new File(pathname);
        // 3. 运行指令
        process = Runtime.getRuntime().exec(cmd, null, file);
        String result = null;
        BufferedInputStream in = new BufferedInputStream(process.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String lineStr = null;
        while ((lineStr = br.readLine()) != null) {
            result = lineStr;
            System.out.println(lineStr);
        }
        br.close();
        in.close();
        process.waitFor();
    }

    @Test
    void initTask(){
        String taskID = "Assemble/abscass";
        String taskDir = "../../bioRepository/user_dir/" + taskID;
        File file = new File(taskDir);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("------>建立任务目录：" + file);
        }
    }

    @Test
    void testReader() throws IOException {
            // 读取得从本地读取 这里要由绝对路径转为相对路径
            System.out.println("---> commonService：draftReadMapInfo");
            String mapInfoUrl = "../../bioRepository/user_dir/Assemble/cf4c953b-f0ce-48c0-9408-b890bee8c715/Quartet_contig.mapinfo";
            System.out.println("------>mapInfoUrl:" + mapInfoUrl);
            DraftMapInfoResult draftMapInfoResult = new DraftMapInfoResult();
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
                }else{ // # 统计信息
                    if(line.contains("Total mapped")){
                        draftMapInfoResult.setTotalMapped(line.split("#")[1]);
                    }
                    if(line.contains("Total discarded")){
                        draftMapInfoResult.setTotalDiscarded(line.split("#")[1]);
                    }
                }
                line = reader.readLine();
            }
        reader.close();
        draftMapInfoResult.setData(draftMapInfos);
        System.out.println(draftMapInfoResult);
    }


}
