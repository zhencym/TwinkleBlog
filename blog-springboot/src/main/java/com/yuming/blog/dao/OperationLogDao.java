package com.yuming.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuming.blog.entity.OperationLog;
import org.springframework.stereotype.Repository;


@Repository
public interface OperationLogDao extends BaseMapper<OperationLog> {
}
