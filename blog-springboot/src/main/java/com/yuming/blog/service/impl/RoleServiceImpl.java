package com.yuming.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.dao.RoleDao;
import com.yuming.blog.dao.UserRoleDao;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.RoleDTO;
import com.yuming.blog.dto.UserRoleDTO;
import com.yuming.blog.entity.Role;
import com.yuming.blog.entity.RoleMenu;
import com.yuming.blog.entity.RoleResource;
import com.yuming.blog.entity.UserRole;
import com.yuming.blog.exception.ServeException;
import com.yuming.blog.login.SessionManager;
import com.yuming.blog.login.UserFilterMetadata;
import com.yuming.blog.service.RoleMenuService;
import com.yuming.blog.service.RoleResourceService;
import com.yuming.blog.service.RoleService;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class RoleServiceImpl extends ServiceImpl<RoleDao, Role> implements RoleService {
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private RoleResourceService roleResourceService;
    @Autowired
    private RoleMenuService roleMenuService;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private UserFilterMetadata userFilterMetadata;

    @Override
    public List<UserRoleDTO> listUserRoles() {
        // 查询角色列表
        List<Role> roleList = roleDao.selectList(new LambdaQueryWrapper<Role>()
                .select(Role::getId, Role::getRoleName));
        return BeanCopyUtil.copyList(roleList, UserRoleDTO.class);
    }

    /**
     * 根据条件查询当前角色列表
     * @param conditionVO 条件
     * @return
     */
    @Override
    public PageDTO<RoleDTO> listRoles(ConditionVO conditionVO) {
        // 转换页码
        conditionVO.setCurrent((conditionVO.getCurrent() - 1) * conditionVO.getSize());
        // 查询角色列表
        List<RoleDTO> roleDTOList = roleDao.listRoles(conditionVO);
        // 查询总量
        Integer count = roleDao.selectCount(new LambdaQueryWrapper<Role>()
                .like(StringUtils.isNotBlank(conditionVO.getKeywords()), Role::getRoleName, conditionVO.getKeywords()));
        return new PageDTO<>(roleDTOList, count);
    }

    /**
     * 同时完成更新角色名、更新角色-权限映射、更新角色-菜单映射的功能
     * @param roleVO 角色
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateRole(RoleVO roleVO) {
        // 判断角色名重复
        Integer count = roleDao.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, roleVO.getRoleName()));
        if (Objects.isNull(roleVO.getId()) && count > 0) {
            throw new ServeException("角色名已存在");
        }
        // 保存或更新角色信息
        Role role = Role.builder()
                .id(roleVO.getId())
                .roleName(roleVO.getRoleName())
                .roleLabel(roleVO.getRoleLabel())
                .createTime(Objects.isNull(roleVO.getId()) ? new Date() : null)
                .updateTime(Objects.nonNull(roleVO.getId()) ? new Date() : null)
                .isDisable(CommonConst.FALSE)
                .build();
        // 保存或更新角色
        this.saveOrUpdate(role);
        // 更新资源列表
        if (CollectionUtils.isNotEmpty(roleVO.getResourceIdList())) {
            //删除旧的角色-权限关系
            if (Objects.nonNull(roleVO.getId())) {
                roleResourceService.remove(new LambdaQueryWrapper<RoleResource>().eq(RoleResource::getRoleId, roleVO.getId()));
            }
            //插入新的角色-权限关系
            List<RoleResource> roleResourceList = roleVO.getResourceIdList().stream()
                    .map(resourceId -> RoleResource.builder()
                            .roleId(role.getId())
                            .resourceId(resourceId)
                            .build())
                    .collect(Collectors.toList());
            // 将新的 角色权限映射关系 保存到数据库
            roleResourceService.saveBatch(roleResourceList);
            // 重新加载角色资源信息
            userFilterMetadata.clearDataSource();
        }

        // 更新菜单列表，也就是该角色所能看到的前端组件
        if (CollectionUtils.isNotEmpty(roleVO.getMenuIdList())) {
            if (Objects.nonNull(roleVO.getId())) {
                //删除旧的 角色-菜单映射
                roleMenuService.remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleVO.getId()));
            }
            List<RoleMenu> roleMenuList = roleVO.getMenuIdList().stream()
                    .map(menuId -> RoleMenu.builder()
                            .roleId(role.getId())
                            .menuId(menuId)
                            .build())
                    .collect(Collectors.toList());
            //插入新的 角色-菜单映射
            roleMenuService.saveBatch(roleMenuList);
        }
    }

    /**
     * 删除角色，先判断角色下有无用户
     * @param roleIdList 角色id列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRoles(List<Integer> roleIdList) {
        // 判断角色下是否有用户
        Integer count = userRoleDao.selectCount(new LambdaQueryWrapper<UserRole>()
                .in(UserRole::getRoleId, roleIdList));
        if (count > 0) {
            throw new ServeException("该角色下存在用户");
        }
        roleDao.deleteBatchIds(roleIdList);
    }

}
