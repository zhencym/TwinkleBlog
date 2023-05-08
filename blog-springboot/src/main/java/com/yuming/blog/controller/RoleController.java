package com.yuming.blog.controller;

import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.UserRoleDTO;
import com.yuming.blog.service.RoleService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.Result;
import com.yuming.blog.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 角色
 **/
@RestController
public class RoleController {
    @Autowired
    private RoleService roleService;

    /**
     * 查询用户角色选项
     * @return
     */
    @GetMapping("/admin/users/role")
    public Result<List<UserRoleDTO>> listUserRole() {
        return new Result<>(true, StatusConst.OK, "查询成功", roleService.listUserRoles());
    }

    /**
     * 查询角色列表
     * @param conditionVO
     * @return
     */
    @GetMapping("/admin/roles")
    public Result<List<UserRoleDTO>> listRoles(ConditionVO conditionVO) {
        return new Result<>(true, StatusConst.OK, "查询成功", roleService.listRoles(conditionVO));
    }

    /**
     * 保存或更新角色
     * @param roleVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.ROLE, optDesc = "保存或更新角色")
    @PostMapping("/admin/role")
    public Result listRoles(@RequestBody @Valid RoleVO roleVO) {
        roleService.saveOrUpdateRole(roleVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 删除角色
     * @param roleIdList
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.ROLE, optDesc = "删除角色")
    @DeleteMapping("/admin/roles")
    public Result deleteRoles(@RequestBody List<Integer> roleIdList) {
        roleService.deleteRoles(roleIdList);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

}
