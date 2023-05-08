package com.yuming.blog.controller;

import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.CommentBackDTO;
import com.yuming.blog.dto.CommentDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.ReplyDTO;
import com.yuming.blog.service.CommentService;
import com.yuming.blog.vo.CommentVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;
import com.yuming.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 评论
 */
@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 查询评论
     * @param articleId 文章id
     * @param current 当前页码
     * @return
     */
    @GetMapping("/comments")
    public Result<PageDTO<CommentDTO>> listComments(Integer articleId, Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功！", commentService.listComments(articleId, current));
    }

    /**
     * 添加评论或回复
     * @param commentVO
     * @return
     */
    @PostMapping("/comments")
    public Result saveComment(@Valid @RequestBody CommentVO commentVO) {
        commentService.saveComment(commentVO);
        return new Result<>(true, StatusConst.OK, "评论成功！");
    }

    /**
     * 查询评论下的回复
     * @param commentId 文章id
     * @param current 当前页码
     * @return
     */
    @GetMapping("/comments/replies/{commentId}")
    public Result<List<ReplyDTO>> listRepliesByCommentId(@PathVariable("commentId") Integer commentId, Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功！", commentService.listRepliesByCommentId(commentId, current));
    }

    /**
     * 评论点赞
     * @param commentId
     * @return
     * @throws InterruptedException
     */
    @PostMapping("/comments/like")
    public Result saveCommentList(Integer commentId) throws InterruptedException {
        commentService.saveCommentLike(commentId);
        return new Result<>(true, StatusConst.OK, "点赞成功！");
    }

    /**
     * 删除或恢复评论
     * @param deleteVO
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.COMMENT, optDesc = "删除或恢复评论")
    @PutMapping("/admin/comments")
    public Result deleteComment(DeleteVO deleteVO) {
        commentService.updateCommentDelete(deleteVO);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

    /**
     * 物理删除评论
     * @param commentIdList
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.COMMENT, optDesc = "物理删除评论")
    @DeleteMapping("/admin/comments")
    public Result deleteComments(@RequestBody List<Integer> commentIdList) {
        commentService.removeByIds(commentIdList);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

    /**
     * 查询后台评论
     * @param condition
     * @return
     */
    @GetMapping("/admin/comments")
    public Result<PageDTO<CommentBackDTO>> listCommentBackDTO(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "查询成功", commentService.listCommentBackDTO(condition));
    }

}

