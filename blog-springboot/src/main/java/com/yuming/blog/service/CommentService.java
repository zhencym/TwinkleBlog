package com.yuming.blog.service;

import com.yuming.blog.dto.CommentBackDTO;
import com.yuming.blog.dto.CommentDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.ReplyDTO;
import com.yuming.blog.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.vo.CommentVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;

import java.util.List;


public interface CommentService extends IService<Comment> {

    /**
     * 查看评论
     * 这个方法会初步显示评论
     * 一级评论显示10条
     * 子评论每组显示3条
     *
     * @param articleId 文章id
     * @param current   当前页码
     * @return CommentListDTO
     */
    PageDTO<CommentDTO> listComments(Integer articleId, Long current);

    /**
     * 查看评论下的回复
     * 这是用于继续查看评论（总评论里回复评论下的评论只显示3条。这里用来显示更多，一页5条）
     *
     * @param commentId 评论id
     * @param current   当前页码
     * @return 回复列表
     */
    List<ReplyDTO> listRepliesByCommentId(Integer commentId, Long current);

    /**
     * 添加评论
     * 在文章下添加评论或者回复他人
     *
     * @param commentVO 评论对象
     */
    void saveComment(CommentVO commentVO);

    /**
     * 点赞评论
     *
     * @param commentId 评论id
     */
    void saveCommentLike(Integer commentId) throws InterruptedException;

    /**
     * 恢复或删除评论
     * 包括批量恢复删除
     *
     * @param deleteVO 逻辑删除对象
     */
    void updateCommentDelete(DeleteVO deleteVO);

    /**
     * 查询后台评论
     * 包括带用户昵称查询
     *
     * @param condition 条件
     * @return 评论列表
     */
    PageDTO<CommentBackDTO> listCommentBackDTO(ConditionVO condition);

}
