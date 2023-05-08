package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.dto.BlogHomeInfoDTO;
import com.yuming.blog.service.BlogInfoService;
import com.yuming.blog.service.impl.WebSocketServiceImpl;
import com.yuming.blog.vo.Result;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.vo.VoiceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 博客信息
 */
@RestController
public class BlogInfoController {
    @Autowired
    private BlogInfoService blogInfoService;
    @Autowired
    private WebSocketServiceImpl webSocketService;

    /**
     * 查看博客信息
     * @return
     */
    @GetMapping("/")
    public Result<BlogHomeInfoDTO> getBlogHomeInfo() {
        return new Result<>(true, StatusConst.OK, "查询成功", blogInfoService.getBlogInfo());
    }

    /**
     * 查看后台信息
     * @return
     */
    @GetMapping("/admin")
    public Result<BlogHomeInfoDTO> getBlogBackInfo() {
        return new Result<>(true, StatusConst.OK, "查询成功", blogInfoService.getBlogBackInfo());
    }

    /**
     * 查看关于我信息
     * @return
     */
    @GetMapping("/about")
    public Result<String> getAbout() {
        return new Result(true, StatusConst.OK, "查询成功", blogInfoService.getAbout());
    }

    /**
     * 修改关于我信息
     * @param aboutContent
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.USER_INFO, optDesc = "修改关于我信息")
    @PutMapping("/admin/about")
    public Result updateAbout(String aboutContent) {
        blogInfoService.updateAbout(aboutContent);
        return new Result<>(true, StatusConst.OK, "修改成功");
    }

    /**
     * 修改公告
     * @param notice
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.USER_INFO, optDesc = "修改公告")
    @PutMapping("/admin/notice")
    public Result updateNotice(String notice) {
        blogInfoService.updateNotice(notice);
        return new Result<>(true, StatusConst.OK, "修改成功");
    }

    /**
     * 上传语音
     * @param voiceVO
     * @return
     * @throws IOException
     */
    @OptLog(optType = OptLogType.UPLOAD, optModule = OptLogModule.OTHER, optDesc = "上传语音")
    @PostMapping("/voice")
    public Result<String> saveVoice(VoiceVO voiceVO) throws IOException {
        webSocketService.sendVoice(voiceVO);
        return new Result<>(true, StatusConst.OK, "上传成功");
    }

    /**
     * 查看公告
     * @return
     */
    @GetMapping("/admin/notice")
    public Result<String> getNotice() {
        return new Result(true, StatusConst.OK, "查看成功", blogInfoService.getNotice());
    }

}

