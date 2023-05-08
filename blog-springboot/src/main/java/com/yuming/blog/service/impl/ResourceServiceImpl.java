package com.yuming.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.dao.ResourceDao;
import com.yuming.blog.dao.RoleResourceDao;
import com.yuming.blog.dto.ResourceDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.entity.Resource;
import com.yuming.blog.entity.RoleResource;
import com.yuming.blog.exception.ServeException;
import com.yuming.blog.login.UserFilterMetadata;
import com.yuming.blog.service.ResourceService;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.vo.ResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceDao, Resource> implements ResourceService {

    @Autowired
    private RoleResourceDao roleResourceDao;

    @Autowired
    private UserFilterMetadata userFilterMetadata;

    @Override
    public void saveOrUpdateResource(ResourceVO resourceVO) {
        // 更新资源信息
        Resource resource = BeanCopyUtil.copyObject(resourceVO, Resource.class);
        resource.setCreateTime(Objects.isNull(resource.getId()) ? new Date() : null);
        resource.setUpdateTime(Objects.nonNull(resource.getId()) ? new Date() : null);
        // 先清除，等待重新加载角色资源信息； 懒加载，但是线程不安全
        userFilterMetadata.clearDataSource();
        this.saveOrUpdate(resource);
    }

    @Override
    public void deleteResources(List<Integer> resourceIdList) {
        // 查询是否有角色关联
        Integer count = roleResourceDao.selectCount(new LambdaQueryWrapper<RoleResource>()
                .in(RoleResource::getResourceId, resourceIdList));
        if (count > 1) {
            throw new ServeException("该资源下存在角色");
        }
        this.removeByIds(resourceIdList);
    }

    @Override
    public List<ResourceDTO> listResources() {
        // 查询资源列表
        List<Resource> resourceList = this.list(null);
        // 获取所有模块
        List<Resource> parentList = listResourceModule(resourceList);
        // 根据父id分组获取模块下的资源
        Map<Integer, List<Resource>> childrenMap = listResourceChildren(resourceList);
        // 绑定模块下的所有接口
        return parentList.stream().map(item -> {
            ResourceDTO resourceDTO = BeanCopyUtil.copyObject(item, ResourceDTO.class);
            List<ResourceDTO> childrenList = BeanCopyUtil.copyList(childrenMap.get(item.getId()), ResourceDTO.class);
            resourceDTO.setChildren(childrenList);
            return resourceDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<labelOptionDTO> listResourceOption() {
        // 查询资源列表
        List<Resource> resourceList = this.list(new LambdaQueryWrapper<Resource>()
                .select(Resource::getId, Resource::getResourceName, Resource::getParentId)
                .eq(Resource::getIsAnonymous, CommonConst.FALSE)
                .eq(Resource::getIsDisable, CommonConst.FALSE));
        // 获取所有模块（目录模块）
        List<Resource> parentList = listResourceModule(resourceList);
        // 根据父id分组获取模块下的资源 （资源模块）
        Map<Integer, List<Resource>> childrenMap = listResourceChildren(resourceList);
        // 组装父子数据
        return parentList.stream().map(item -> {
            List<labelOptionDTO> list = new ArrayList<>();
            List<Resource> children = childrenMap.get(item.getId());
            if (Objects.nonNull(children)) {
                list = children.stream()
                        .map(resource -> labelOptionDTO.builder()
                                .id(resource.getId())
                                .label(resource.getResourceName())
                                .build())
                        .collect(Collectors.toList());
            }
            return labelOptionDTO.builder()
                    .id(item.getId())
                    .label(item.getResourceName())
                    .children(list)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 获取模块下的所有资源
     * 同一个parent_id的分到同一组
     *
     * @param resourceList 资源列表
     * @return 模块资源
     */
    private Map<Integer, List<Resource>> listResourceChildren(List<Resource> resourceList) {
        return resourceList.stream()
                .filter(item -> Objects.nonNull(item.getParentId()))
                .collect(Collectors.groupingBy(Resource::getParentId));
    }

    /**
     * 获取所有资源模块
     * 过滤得到没有parent_id的
     * @param resourceList 资源列表
     * @return 资源模块列表
     */
    private List<Resource> listResourceModule(List<Resource> resourceList) {
        return resourceList.stream()
                .filter(item -> Objects.isNull(item.getParentId()))
                .collect(Collectors.toList());
    }

}
