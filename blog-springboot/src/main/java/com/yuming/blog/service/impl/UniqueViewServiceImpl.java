package com.yuming.blog.service.impl;


import com.yuming.blog.dto.UniqueViewDTO;
import com.yuming.blog.entity.UniqueView;
import com.yuming.blog.dao.UniqueViewDao;
import com.yuming.blog.service.UniqueViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
public class UniqueViewServiceImpl extends ServiceImpl<UniqueViewDao, UniqueView> implements UniqueViewService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UniqueViewDao uniqueViewDao;

    /**
     * 定时任务
     * 把每天的用户量都存入unique_View表,用户量就是当天不同的IP数量，存在redis的ip集合中。
     * 每天0点定时执行。存完之后，每天0点10分定时执行清空redis中的ip集合
     * 应该先保存，再清空，两者应该放在同一个定时方法里更合理
     */
    @Scheduled(cron = " 0 0 0 * * ?")  //每天0点执行一次（秒 分 时 日 月 周 ）
    @Override
    public void saveUniqueView() {
        // 获取每天用户量
        Long count = redisTemplate.boundSetOps("ip_set").size();
        // 获取昨天日期插入数据
        UniqueView uniqueView = UniqueView.builder()
                .createTime(DateUtil.getSomeDay(new Date(), -1))
                .viewsCount(Objects.nonNull(count) ? count.intValue() : 0).build();
        uniqueViewDao.insert(uniqueView);
    }


    @Override
    public List<UniqueViewDTO> listUniqueViews() {
        String startTime = DateUtil.getMinTime(DateUtil.getSomeDay(new Date(), -7));
        String endTime = DateUtil.getMaxTime(new Date());
        return uniqueViewDao.listUniqueViews(startTime, endTime);
    }

}
