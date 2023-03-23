package com.yuming.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.dto.MenuDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.dto.UserMenuDTO;
import com.yuming.blog.entity.Menu;
import com.yuming.blog.vo.ConditionVO;

import java.util.List;


public interface MenuService extends IService<Menu> {

    /**
     * 查看菜单列表
     * @param conditionVO 条件
     * @return 菜单列表
     */
    List<MenuDTO> listMenus(ConditionVO conditionVO);

    /**
     * 查看角色菜单选项
     * 这是角色的前台后台页面的权限管理
     * @return 角色菜单选项
     */
    List<labelOptionDTO> listMenuOptions();

    /**
     * 查看用户菜单
     * 即查询用户能看到的菜单选项
     * @return 菜单列表
     */
    List<UserMenuDTO> listUserMenus();

}
