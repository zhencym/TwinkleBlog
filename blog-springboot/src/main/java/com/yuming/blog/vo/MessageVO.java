package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 留言VO
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO {

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 头像
     */
    @NotBlank(message = "头像不能为空")
    private String avatar;

    /**
     * 留言内容
     */
    @NotBlank(message = "留言内容不能为空")
    private String messageContent;

    /**
     * 弹幕速度
     */
    @NotNull(message = "弹幕速度不能为空")
    private Integer time;
}
