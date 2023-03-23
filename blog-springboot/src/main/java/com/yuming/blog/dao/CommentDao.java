package com.yuming.blog.dao;

import com.yuming.blog.dto.CommentBackDTO;
import com.yuming.blog.dto.CommentDTO;
import com.yuming.blog.dto.ReplyCountDTO;
import com.yuming.blog.dto.ReplyDTO;
import com.yuming.blog.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuming.blog.vo.ConditionVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentDao extends BaseMapper<Comment> {

    /**
     * 查看评论
     * 根据文章id得到所有评论（限制10条），或者不需要文章id得到所有评论（一级评论）
     *
     * @param articleId 文章id
     * @param current   当前页码
     * @return 评论集合
     */
    List<CommentDTO> listComments(@Param("articleId") Integer articleId, @Param("current") Long current);

    /**
     * 查看评论id集合下的回复
     * （查看回复评论的评论，parent_id有值,二级评论，每组显示3条）
     * @param commentIdList 评论id集合
     * @return 回复集合
     */
    List<ReplyDTO> listReplies(@Param("commentIdList") List<Integer> commentIdList);

    /**
     * 查看当条评论下的回复
     * 查看更多子评论（之前是3条）
     * 查看的更多后，每页5条
     *
     * @param commentId 评论id
     * @param current   当前页码
     * @return 回复集合
     */
    List<ReplyDTO> listRepliesByCommentId(@Param("commentId") Integer commentId, @Param("current") Long current);

    /**
     * 根据评论id查询回复总量
     * 从之前的查询一级评论得到一级评论列表id，现在根据每个一级评论查询回复量
     *
     * @param commentIdList 评论id集合
     * @return 回复数量
     */
    List<ReplyCountDTO> listReplyCountByCommentId(@Param("commentIdList") List<Integer> commentIdList);

    /**
     * 查询后台评论
     * 根据是否删除、评论用户用户名查询，或直接列出来所有评论
     *
     * @param condition 条件
     * @return 评论集合
     */
    List<CommentBackDTO> listCommentBackDTO(@Param("condition") ConditionVO condition);

    /**
     * 统计后台评论数量
     * 根据是否删除、查询用户名，来统计条数
     *
     * @param condition 条件
     * @return 评论数量
     */
    Integer countCommentDTO(@Param("condition") ConditionVO condition);

}
