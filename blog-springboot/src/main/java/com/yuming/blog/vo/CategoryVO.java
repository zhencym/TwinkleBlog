package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


/**
 * 分类VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 分类名
     */
    @NotBlank(message = "分类名不能为空")
    private String categoryName;

}
