package com.yuming.blog.controller;

import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.MenuDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.dto.UserMenuDTO;
import com.yuming.blog.service.MenuService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单
 **/
@RestController
public class MenuController {
    @Autowired
    private MenuService menuService;

    /**
     * 查看菜单列表
     * @param conditionVO
     * @return
     */
    @GetMapping("/admin/menus")
    public Result<List<MenuDTO>> listMenus(ConditionVO conditionVO) {
        return new Result<>(true, StatusConst.OK, "查询成功", menuService.listMenus(conditionVO));
    }

    /**
     * 查看角色菜单选项
     * @return
     */
    @GetMapping("/admin/role/menus")
    public Result<List<labelOptionDTO>> listMenuOptions() {
        return new Result<>(true, StatusConst.OK, "查询成功", menuService.listMenuOptions());
    }

    /**
     * 查看用户菜单
     * @return
     */
    @GetMapping("/admin/user/menus")
    public Result<List<UserMenuDTO>> listUserMenus() {
        return new Result<>(true, StatusConst.OK, "查询成功", menuService.listUserMenus());
    }

}
