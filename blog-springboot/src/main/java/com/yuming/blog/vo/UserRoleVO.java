package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户角色VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleVO {
    /**
     * 用户id
     */
    @NotNull(message = "id不能为空")
    private Integer userInfoId;

    /**
     * 用户昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 用户角色
     */
    @NotNull(message = "用户角色不能为空")
    private List<Integer> roleIdList;

}
