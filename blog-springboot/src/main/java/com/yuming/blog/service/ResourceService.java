package com.yuming.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.dto.ResourceDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.entity.Resource;
import com.yuming.blog.vo.ResourceVO;

import java.util.List;


public interface ResourceService extends IService<Resource> {


    /**
     * 添加或修改资源
     * @param resourceVO 资源对象
     */
    void saveOrUpdateResource(ResourceVO resourceVO);

    /***
     * 删除资源
     * @param resourceIdList 资源id列表
     */
    void deleteResources(List<Integer> resourceIdList);

    /**
     * 查看资源列表
     * 资源管理中的资源列表
     *
     * @return 资源列表
     */
    List<ResourceDTO> listResources();

    /**
     * 查看资源选项
     * 角色管理中的资源选项
     * @return 资源选项
     */
    List<labelOptionDTO> listResourceOption();

}
