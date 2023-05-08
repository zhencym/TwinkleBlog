package com.yuming.blog.controller;

import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.ResourceDTO;
import com.yuming.blog.dto.labelOptionDTO;
import com.yuming.blog.service.ResourceService;
import com.yuming.blog.vo.ResourceVO;
import com.yuming.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 资源
 **/
@RestController
public class ResourceController {
    @Autowired
    private ResourceService resourceService;

    /**
     * 查看资源列表
     * @return
     */
    @GetMapping("/admin/resources")
    public Result<List<ResourceDTO>> listResources() {
        return new Result<>(true, StatusConst.OK, "查询成功", resourceService.listResources());
    }

    /**
     * 删除资源
     * @param resourceIdList
     * @return
     */
    @DeleteMapping("/admin/resources")
    public Result deleteResources(@RequestBody List<Integer> resourceIdList) {
        resourceService.deleteResources(resourceIdList);
        return new Result<>(true, StatusConst.OK, "删除成功");
    }

    /**
     * 新增或修改资源
     * @param resourceVO
     * @return
     */
    @PostMapping("/admin/resources")
    public Result saveOrUpdateResource(@RequestBody @Valid ResourceVO resourceVO) {
        resourceService.saveOrUpdateResource(resourceVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 查看角色资源选项
     * @return
     */
    @GetMapping("/admin/role/resources")
    public Result<List<labelOptionDTO>> listResourceOption() {
        return new Result<>(true, StatusConst.OK, "查询成功", resourceService.listResourceOption());
    }


}
