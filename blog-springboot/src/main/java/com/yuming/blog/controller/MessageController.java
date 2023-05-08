package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.MessageBackDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.MessageDTO;
import com.yuming.blog.service.MessageService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.MessageVO;
import com.yuming.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 留言
 */
@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;

    /**
     * 添加留言
     * @param messageVO
     * @return
     */
    @PostMapping("/messages")
    public Result saveMessage(@Valid @RequestBody MessageVO messageVO) {
        messageService.saveMessage(messageVO);
        return new Result<>(true, StatusConst.OK, "添加成功");
    }

    /**
     * 查看留言列表
     * @return
     */
    @GetMapping("/messages")
    public Result<List<MessageDTO>> listMessages() {
        return new Result<>(true, StatusConst.OK, "添加成功", messageService.listMessages());
    }

    /**
     * 查看后台留言列表
     * @param condition
     * @return
     */
    @GetMapping("/admin/messages")
    public Result<PageDTO<MessageBackDTO>> listMessageBackDTO(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "添加成功", messageService.listMessageBackDTO(condition));
    }

    /**
     * 删除留言
     * @param messageIdList 删除留言列表id
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.LEAVE_MESSAGE, optDesc = "删除留言")
    @DeleteMapping("/admin/messages")
    public Result deleteMessages(@RequestBody List<Integer> messageIdList) {
        messageService.removeByIds(messageIdList);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

}

