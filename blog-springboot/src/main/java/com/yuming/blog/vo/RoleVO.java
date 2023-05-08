package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色VO
 *
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleVO {
    /**
     * id
     */
    private Integer id;

    /**
     * 标签名
     */
    @NotBlank(message = "角色名不能为空")
    private String roleName;

    /**
     * 标签名
     */
    @NotBlank(message = "权限标签不能为空")
    private String roleLabel;

    /**
     * 资源列表
     */
    private List<Integer> resourceIdList;

    /**
     * 菜单列表
     */
    private List<Integer> menuIdList;

}
