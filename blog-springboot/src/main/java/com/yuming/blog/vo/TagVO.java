package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 标签VO
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 标签名
     */
    @NotBlank(message = "标签名不能为空")
    private String tagName;

}
