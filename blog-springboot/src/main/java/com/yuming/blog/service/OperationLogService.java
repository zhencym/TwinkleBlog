package com.yuming.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.dto.OperationLogDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.entity.OperationLog;
import com.yuming.blog.vo.ConditionVO;


public interface OperationLogService extends IService<OperationLog> {

    /**
     * 查询日志列表
     *
     * @param conditionVO 条件
     * @return 日志列表
     */
    PageDTO<OperationLogDTO> listOperationLogs(ConditionVO conditionVO);

}
