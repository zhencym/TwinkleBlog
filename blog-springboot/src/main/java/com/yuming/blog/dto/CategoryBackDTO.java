package com.yuming.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台分类列表
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBackDTO {
    /**
     * id
     */
    private Integer id;

    /**
     * 分类名
     */
    private String categoryName;
}
