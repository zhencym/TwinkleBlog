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
import com.yuming.blog.handler.FilterInvocationSecurityMetadataSourceImpl;
import com.yuming.blog.service.ResourceService;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.vo.ResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceDao, Resource> implements ResourceService {

    /**
     * Http相应模板，RestTemplate
     * 简化了与http服务的通信方式，统一了RESTful的标准，封装了http链接， 我们只需要传入url及返回值类型即可
     */
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RoleResourceDao roleResourceDao;
    /**
     * 储存应用中受保护的资源的安全元数据
     */
    @Autowired
    private FilterInvocationSecurityMetadataSourceImpl filterInvocationSecurityMetadataSource;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importSwagger() {
        // 删除所有资源
        this.remove(null);
        roleResourceDao.delete(null);

        List<Resource> resourceList = new ArrayList<>();
        // 指定返回json数据格式：map
        Map<String, Object> data = restTemplate.getForObject("http://localhost:8088/v2/api-docs", Map.class);

        // 获取所有模块（没有parent_id的那种）
        List<Map<String, String>> tagList = (List<Map<String, String>>) data.get("tags");//tags就是模块信息
        tagList.forEach(item -> {
            Resource resource = Resource.builder()
                    .resourceName(item.get("name"))
                    .createTime(new Date())
                    .updateTime(new Date())
                    .isDisable(CommonConst.FALSE)
                    .isAnonymous(CommonConst.FALSE)
                    .build();
            resourceList.add(resource);
        });
        this.saveBatch(resourceList);

        Map<String, Integer> permissionMap = resourceList.stream()
                .collect(Collectors.toMap(Resource::getResourceName, Resource::getId));
        resourceList.clear();
        // 获取所有接口（有parent_id的那种）
        Map<String, Map<String, Map<String, Object>>> path = (Map<String, Map<String, Map<String, Object>>>) data.get("paths");
        path.forEach((url, value) -> value.forEach((requestMethod, info) -> {
            String permissionName = info.get("summary").toString();
            List<String> tag = (List<String>) info.get("tags");
            Integer parentId = permissionMap.get(tag.get(0));
            Resource resource = Resource.builder()
                    .resourceName(permissionName)
                    .url(url.replaceAll("\\{[^}]*\\}", "*"))
                    .parentId(parentId)
                    .requestMethod(requestMethod.toUpperCase())
                    .isDisable(CommonConst.FALSE)
                    .isAnonymous(CommonConst.FALSE)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            resourceList.add(resource);
        }));
        this.saveBatch(resourceList);
    }



    @Override
    public void saveOrUpdateResource(ResourceVO resourceVO) {
        // 更新资源信息
        Resource resource = BeanCopyUtil.copyObject(resourceVO, Resource.class);
        resource.setCreateTime(Objects.isNull(resource.getId()) ? new Date() : null);
        resource.setUpdateTime(Objects.nonNull(resource.getId()) ? new Date() : null);
        // 重新加载角色资源信息
        filterInvocationSecurityMetadataSource.clearDataSource();
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
