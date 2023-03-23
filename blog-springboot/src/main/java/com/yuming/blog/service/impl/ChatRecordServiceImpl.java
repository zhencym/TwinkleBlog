package com.yuming.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.dao.ChatRecordDao;
import com.yuming.blog.entity.ChatRecord;
import com.yuming.blog.service.ChatRecordService;
import com.yuming.blog.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordDao, ChatRecord> implements ChatRecordService {
    @Autowired
    private ChatRecordDao chatRecordDao;

    @Override
    public void deleteChartRecord() {
        String time = DateUtil.getMinTime(DateUtil.getSomeDay(new Date(), -7));
        chatRecordDao.delete(new LambdaQueryWrapper<ChatRecord>()
                .le(ChatRecord::getCreateTime,time));
    }

}
