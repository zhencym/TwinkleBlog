package com.yuming.blog.service;

import com.yuming.blog.dto.MessageBackDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.MessageVO;
import com.yuming.blog.dto.MessageDTO;
import com.yuming.blog.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface MessageService extends IService<Message> {

    /**
     * 添加留言弹幕
     *
     * @param messageVO 留言对象
     */
    void saveMessage(MessageVO messageVO);

    /**
     * 查看留言弹幕
     *
     * @return 留言列表
     */
    List<MessageDTO> listMessages();

    /**
     * 查看后台留言
     *
     * @param condition 条件
     * @return 留言列表
     */
    PageDTO<MessageBackDTO> listMessageBackDTO(ConditionVO condition);

}
