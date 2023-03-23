package com.yuming.blog.dao;

import com.yuming.blog.dto.UniqueViewDTO;
import com.yuming.blog.entity.UniqueView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UniqueViewDao extends BaseMapper<UniqueView> {

    /**
     * 获取7天用户量
     * 自定义起点和终点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户量
     */
    List<UniqueViewDTO> listUniqueViews(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
