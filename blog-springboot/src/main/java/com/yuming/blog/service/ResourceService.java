package com.yuming.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.dto.ResourceDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.entity.Resource;
import com.yuming.blog.vo.ResourceVO;

import java.util.List;


public interface ResourceService extends IService<Resource> {

    /**
     * 导入swagger权限
     * 每次启动都从从swagger文档中解析导入模块，实现动态的接口的管理。
     * 即每次修改接口后重新启动，都会刷新重新载入数据库resources表
     * 那我新增的资源，在重启后，不是重新从swagger文档中读取，新增的也没了？
     */
    void importSwagger();

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
