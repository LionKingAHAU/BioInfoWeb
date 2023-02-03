package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.ProcessError;
import com.ahau.domain.assemble.DraftResultUrl;
import com.ahau.domain.centro.CentroCandidate;
import com.ahau.domain.centro.CentroResultUrl;
import com.ahau.domain.gapFill.GapResultUrl;
import com.ahau.domain.telo.TeloResultUrl;
import com.ahau.exception.BusinessException;
import com.ahau.service.impl.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Vector;

@RestController
@RequestMapping("/blast/common") // 对应前端的格式
public class CommonController {


    // 注入commonService
    @Autowired
    private CommonService commonService;


    /**
     * @Description: 把用户的邮箱存储在Session中
     * @Param: String value
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/setEmail/{value}")
    public Result setEmail(HttpServletRequest request, @PathVariable String value) {
        System.out.println("===》CommonController: setEmail......");
        // 1. 获取Session 设置邮箱
        HttpSession session = request.getSession();
        System.out.println("---》Email is: " + value);
        session.setAttribute("Email", value);
        return new Result(Code.SEND_OK, "success");
    }


    /**
     * @Description: Assemble上传Genome文件
     * @Param: HttpServletRequest 前端发送的请求
     * @Param: MultipartFile file实例
     * @Return: Result 回复前端的响应体
     */
    @PostMapping("/uploadAssembleGenome")
    public Result uploadAssembleTrain(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: upload\tAssemble\tGenome......");
        // 1. 转存文件
        String uuidName = commonService.upload(file);
        HttpSession session = request.getSession();
        // 2. 设置Attribute name
        String paramType = "AssembleGenome";
        session.setAttribute(paramType, uuidName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: Assemble删除Genome文件
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/removeAssembleGenome")
    public Result removeAssembleGenome(HttpServletRequest request) {
        System.out.println("===》CommonController: removeFile......");
        String paramType = "AssembleGenome";
        Boolean remove = commonService.remove(request, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: Assemble上传HiFi文件
     * @Param: HttpServletRequest
     * @Param: MultipartFile
     * @Return: Result
     */
    @PostMapping("/uploadAssembleHiFi")
    public Result uploadAssembleHiFi(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: upload\tAssemble\tHiFi......");
        // 1. 转存文件
        String uuidName = commonService.upload(file);
        HttpSession session = request.getSession();
        // 2. 设置Attribute name
        String paramType = "AssembleHiFi";
        session.setAttribute(paramType, uuidName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: Assemble删除HiFi文件
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/removeAssembleHiFi")
    public Result removeAssembleHiFi(HttpServletRequest request) {
        System.out.println("===》CommonController: remove\t HiFi\t File......");
        String paramType = "AssembleHiFi";
        Boolean remove = commonService.remove(request, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: 数据回显 从Session中获取展示数据，返回给前端，结合mounted使用
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/draftDisplay")
    public Result draftDisplay(HttpServletRequest request) {
        try {
            System.out.println("===》CommonController: draftDisplay......");
            DraftResultUrl draftResultUrl = commonService.assembleDisplay(request);
            return new Result(Code.TRAIN_OK, "success", draftResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }


    /**
     * @Description: GapFill上传Genome文件
     * @Param: HttpServletRequest
     * @Param: MultipartFile
     * @Return: Result
     */
    @PostMapping("/uploadFillGenome")
    public Result uploadFillGenome(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: upload\tfill\tGenome......");
        // 1. 转存文件
        String uuidName = commonService.upload(file);
        HttpSession session = request.getSession();
        // 2. 设置Attribute name
        String paramType = "GapFillGenome";
        session.setAttribute(paramType, uuidName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: GapFill删除Genome文件
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/removeFillGenome")
    public Result removeFillGenome(HttpServletRequest request) {
        System.out.println("===》CommonController: remove\t HiFi\t File......");
        String paramType = "GapFillGenome";
        Boolean remove = commonService.remove(request, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: GapFill上传多个Contigs文件
     * @Param: HttpServletRequest
     * @Param: MultipartFile
     * @Return: Result
     */
    @PostMapping("/uploadFillContig")
    public Result uploadFillContigs(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: upload\tfill\tGontigs......");
        String paramType = "GapFillContigs";
        return commonService.multiUpload(file, request, paramType);
    }


    /**
     * @Description: GapFill删除Contigs文件
     * @Param: HttpServletRequest
     * @Param: String filename 前端对应的文件名字
     * @Return: Result
     */
    @GetMapping("/removeFillContig/{filename}")
    public Result removeContig(HttpServletRequest request, @PathVariable String filename) {
        System.out.println("===》CommonController: removeFile......");
        String paramType = "GapFillContigs";
        Boolean remove = commonService.multiRemove(request, filename, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: fillDisplay
     * @Param: httpRequest
     * @Return: Result
     */
    @GetMapping("/fillDisplay")
    public Result fillDisplay(HttpServletRequest request) {
        try {
            System.out.println("===》CommonController: FillDisplay......");
            GapResultUrl gapResultUrl = commonService.gapFillDisplay(request);
            return new Result(Code.TRAIN_OK, "success", gapResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }


    /**
     * @Description: Telo的文件上传
     * @Param: MultiPartFile file HttpRequest
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @PostMapping("/uploadTeloGenome")
    public Result uploadTeloGenome(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: upload\tTelo\tGenome......");
        // 1. 转存文件
        String uuidName = commonService.upload(file);
        HttpSession session = request.getSession();
        // 2. 设置Attribute name
        String paramType = "TeloGenome";
        session.setAttribute(paramType, uuidName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: Telo的删除文件
     * @Param: HttpServletRequest request
     * @Return: Result
     */
    @GetMapping("/removeTeloGenome")
    public Result removeTeloGenome(HttpServletRequest request) {
        System.out.println("===》CommonController: Telo\tRemove\tFile......");
        String paramType = "TeloGenome";
        Boolean remove = commonService.remove(request, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: Telo的display
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/teloDisplay")
    public Result teloDisplay(HttpServletRequest request) {
        try {
            System.out.println("===》CommonController: teloDisplay......");
            TeloResultUrl teloResultUrl = commonService.teloDisplay(request);
            return new Result(Code.TRAIN_OK, "success", teloResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }


    /**
     * @Description: Centro的Genome上传
     * @Param: HttpServletRequest
     * @Param: MultiPartFile
     * @Return: Result
     */
    @PostMapping("/uploadCentroGenome")
    public Result uploadCentroGenome(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: CentroGenome......");
        // 1. 转存文件
        String uuidName = commonService.upload(file);
        HttpSession session = request.getSession();
        // 2. 设置Attribute name
        String paramType = "CentroGenome";
        session.setAttribute(paramType, uuidName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: CentroGenome的删除
     * @Param: HttpServletRequest
     * @Return: Result
     */

    @GetMapping("/removeCentroGenome")
    public Result removeCentroGenome(HttpServletRequest request) {
        System.out.println("===》CommonController: Centro\tRemove\tFile......");
        String paramType = "CentroGenome";
        Boolean remove = commonService.remove(request, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: Centro TE annotation的上传
     * @Param: HttpServletRequest
     * @Param: MultipartFile
     * @Return: Result
     */

    @PostMapping("/uploadCentroTE")
    public Result uploadCentroTE(HttpServletRequest request, MultipartFile file) {
        System.out.println("===》CommonController: CentroTE......");
        // 1. 转存文件
        String uuidName = commonService.upload(file);
        HttpSession session = request.getSession();
        // 2. 设置Attribute name
        String paramType = "CentroTE";
        session.setAttribute(paramType, uuidName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: Centro TE annotation的删除
     * @Param: HttpServletRequest
     * @Return: Result
     */

    @GetMapping("/removeCentroTE")
    public Result removeCentroTE(HttpServletRequest request) {
        System.out.println("===》CommonController: Centro\tremoveTE......");
        String paramType = "CentroTE";
        Boolean remove = commonService.remove(request, paramType);
        return new Result(Code.TRAIN_OK, "success", remove);
    }


    /**
     * @Description: Centro的display
     * @Param: HttpServletRequest
     * @Return: ResultUrl
     */
    @GetMapping("/centroDisplay")
    public Result centroDisplay(HttpServletRequest request) {
        try {
            System.out.println("===》CommonController: centroDisplay......");
            CentroResultUrl centroResultUrl = commonService.centroDisplay(request);
            return new Result(Code.TRAIN_OK, "success", centroResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }


    /**
     * @Description: Centro的Show More查看表格功能
     * @Param: HttpServletRequest
     * @Param: String fileName
     * @Return: Result
     */
    @GetMapping("/centroShowMore/{fileName}")
    public Result centroShowMore(HttpServletRequest request, @PathVariable String fileName) {
        System.out.println("===》CommonController: centroShowMore......");
        ArrayList<CentroCandidate> centroCandidates = commonService.centroShowMore(request, fileName);
        return new Result(Code.CENTRO_SEARCH_OK, "success", centroCandidates);
    }


    /**
     * @Description: Err页面展示运行的错误
     * @Param: HttpServletRequest
     * @Return: Result
     */
    @GetMapping("/errDisplay")
    public Result errDisplay(HttpServletRequest request) {
        System.out.println("===》CommonController: errDisplay......");
        HttpSession session = request.getSession();
        String taskID = (String) session.getAttribute("TaskID"); // Assemble/uuid/
        String catalogue = taskID.split("/")[0] + "/";
        String paramType = catalogue + "Errors";
        Vector<ProcessError> processErrors = (Vector<ProcessError>) session.getAttribute(paramType);
        System.out.println(processErrors);
        return new Result(Code.TRAIN_ERR, "errors", processErrors);
    }

}
