package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 友链VO
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendLinkVO {
    /**
     * id
     */
    private Integer id;

    /**
     * 链接名
     */
    @NotBlank(message = "链接名不能为空")
    private String linkName;

    /**
     * 链接头像
     */
    @NotBlank(message = "链接头像不能为空")
    private String linkAvatar;

    /**
     * 链接地址
     */
    @NotBlank(message = "链接地址不能为空")
    private String linkAddress;

    /**
     * 介绍
     */
    @NotBlank(message = "链接介绍不能为空")
    private String linkIntro;

}
