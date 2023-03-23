package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.dto.BlogHomeInfoDTO;
import com.yuming.blog.service.BlogInfoService;
import com.yuming.blog.service.impl.WebSocketServiceImpl;
import com.yuming.blog.vo.Result;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.vo.VoiceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.yuming.blog.constant.OptTypeConst.UPDATE;

/**
 * 博客信息
 */
@Api(tags = "博客信息模块")
@RestController
public class BlogInfoController {
    @Autowired
    private BlogInfoService blogInfoService;
    @Autowired
    private WebSocketServiceImpl webSocketService;

    @ApiOperation(value = "查看博客信息")
    @GetMapping("/")
    public Result<BlogHomeInfoDTO> getBlogHomeInfo() {
        return new Result<>(true, StatusConst.OK, "查询成功", blogInfoService.getBlogInfo());
    }

    @ApiOperation(value = "查看后台信息")
    @GetMapping("/admin")
    public Result<BlogHomeInfoDTO> getBlogBackInfo() {
        return new Result<>(true, StatusConst.OK, "查询成功", blogInfoService.getBlogBackInfo());
    }

    @ApiOperation(value = "查看关于我信息")
    @GetMapping("/about")
    public Result<String> getAbout() {
        return new Result(true, StatusConst.OK, "查询成功", blogInfoService.getAbout());
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改关于我信息")
    @PutMapping("/admin/about")
    public Result updateAbout(String aboutContent) {
        blogInfoService.updateAbout(aboutContent);
        return new Result<>(true, StatusConst.OK, "修改成功");
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改公告")
    @PutMapping("/admin/notice")
    public Result updateNotice(String notice) {
        blogInfoService.updateNotice(notice);
        return new Result<>(true, StatusConst.OK, "修改成功");
    }

    @ApiOperation(value = "上传语音")
    @ApiImplicitParam(name = "file", value = "语音文件", required = true, dataType = "MultipartFile")
    @PostMapping("/voice")
    public Result<String> saveVoice(VoiceVO voiceVO) throws IOException {
        webSocketService.sendVoice(voiceVO);
        return new Result<>(true, StatusConst.OK, "上传成功");
    }

    @ApiOperation(value = "查看公告")
    @GetMapping("/admin/notice")
    public Result<String> getNotice() {
        return new Result(true, StatusConst.OK, "查看成功", blogInfoService.getNotice());
    }

}

