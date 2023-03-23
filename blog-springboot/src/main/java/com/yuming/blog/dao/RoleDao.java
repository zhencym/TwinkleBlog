package com.yuming.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuming.blog.dto.RoleDTO;
import com.yuming.blog.dto.UrlRoleDTO;
import com.yuming.blog.entity.Role;
import com.yuming.blog.vo.ConditionVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RoleDao extends BaseMapper<Role> {

    /**
     * 查询路由角色列表
     * 查询所有的每种角色（管理、用户、测试）的一级权限，即parent_id=null的这些
     * @return 角色标签
     */
    List<UrlRoleDTO> listUrlRoles();

    /**
     * 根据用户id获取赋予用户的角色列表
     *
     * @param userInfoId 用户id
     * @return 角色标签
     */
    List<String> listRolesByUserInfoId(Integer userInfoId);

    /**
     * 查询角色列表
     * 用于角色管理，列出角色对应的前台权限和后台权限，也可根据角色名来查询
     * @param conditionVO 条件
     * @return 角色列表
     */
    List<RoleDTO> listRoles(@Param("conditionVO") ConditionVO conditionVO);

}
