package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 评论VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {

    /**
     * 回复用户id
     */
    private Integer replyId;

    /**
     * 评论文章id
     */
    private Integer articleId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String commentContent;

    /**
     * 父评论id
     */
    private Integer parentId;

}
