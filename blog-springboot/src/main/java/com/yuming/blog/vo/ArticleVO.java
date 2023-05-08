package com.yuming.blog.vo;


import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 文章VO
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleVO {

    /**
     * 文章id
     */
    private Integer id;

    /**
     * 标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String articleTitle;

    /**
     * 内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String articleContent;

    /**
     * 文章封面
     */
    private String articleCover;

    /**
     * 文章分类
     */
    private Integer categoryId;

    /**
     * 文章标签
     */
    private List<Integer> tagIdList;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否为草稿
     */
    private Integer isDraft;


}
