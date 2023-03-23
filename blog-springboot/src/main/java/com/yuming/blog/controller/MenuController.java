package com.yuming.blog.controller;

import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.MenuDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.dto.UserMenuDTO;
import com.yuming.blog.service.MenuService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单
 **/
@Api(tags = "菜单模块")
@RestController
public class MenuController {
    @Autowired
    private MenuService menuService;

    @ApiOperation(value = "查看菜单列表")
    @GetMapping("/admin/menus")
    public Result<List<MenuDTO>> listMenus(ConditionVO conditionVO) {
        return new Result<>(true, StatusConst.OK, "查询成功", menuService.listMenus(conditionVO));
    }

    @ApiOperation(value = "查看角色菜单选项")
    @GetMapping("/admin/role/menus")
    public Result<List<labelOptionDTO>> listMenuOptions() {
        return new Result<>(true, StatusConst.OK, "查询成功", menuService.listMenuOptions());
    }

    @ApiOperation(value = "查看用户菜单")
    @GetMapping("/admin/user/menus")
    public Result<List<UserMenuDTO>> listUserMenus() {
        return new Result<>(true, StatusConst.OK, "查询成功", menuService.listUserMenus());
    }

}
