package com.yuming.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.entity.ChatRecord;


public interface ChatRecordService extends IService<ChatRecord> {

    /**
     * 删除7天前的聊天记录
     */
    void deleteChartRecord();

}
