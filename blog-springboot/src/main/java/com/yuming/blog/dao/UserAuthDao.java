package com.yuming.blog.dao;

import com.yuming.blog.dto.UserBackDTO;
import com.yuming.blog.entity.UserAuth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuming.blog.vo.ConditionVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserAuthDao extends BaseMapper<UserAuth> {

    /**
     * 查询后台用户列表
     * 用于用户管理页面，查询参数是用户名字，页码，返回size
     * @param condition 条件
     * @return 用户集合
     */
    List<UserBackDTO> listUsers(@Param("condition") ConditionVO condition);

    /**
     * 查询后台用户数量
     * 跟上面一个方法是一起的。统计返回数量
     * @param condition 条件
     * @return 用户数量
     */
    Integer countUser(@Param("condition") ConditionVO condition);

}
