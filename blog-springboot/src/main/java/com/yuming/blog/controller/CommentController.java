package com.yuming.blog.controller;

import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.CommentBackDTO;
import com.yuming.blog.dto.CommentDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.ReplyDTO;
import com.yuming.blog.service.CommentService;
import com.yuming.blog.vo.*;
import com.yuming.blog.vo.CommentVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;
import com.yuming.blog.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.yuming.blog.constant.OptTypeConst.*;

/**
 * 评论
 */
@RestController
@Api(tags = "评论模块")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @ApiOperation(value = "查询评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Long")
    })
    @GetMapping("/comments")
    public Result<PageDTO<CommentDTO>> listComments(Integer articleId, Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功！", commentService.listComments(articleId, current));
    }

    @ApiOperation(value = "添加评论或回复")
    @PostMapping("/comments")
    public Result saveComment(@Valid @RequestBody CommentVO commentVO) {
        commentService.saveComment(commentVO);
        return new Result<>(true, StatusConst.OK, "评论成功！");
    }

    @ApiOperation(value = "查询评论下的回复")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "文章id", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Long")
    })
    @GetMapping("/comments/replies/{commentId}")
    public Result<List<ReplyDTO>> listRepliesByCommentId(@PathVariable("commentId") Integer commentId, Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功！", commentService.listRepliesByCommentId(commentId, current));
    }

    @ApiOperation(value = "评论点赞")
    @PostMapping("/comments/like")
    public Result saveCommentList(Integer commentId) throws InterruptedException {
        commentService.saveCommentLike(commentId);
        return new Result<>(true, StatusConst.OK, "点赞成功！");
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "删除或恢复评论")
    @PutMapping("/admin/comments")
    public Result deleteComment(DeleteVO deleteVO) {
        commentService.updateCommentDelete(deleteVO);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

    @OptLog(optType = REMOVE)
    @ApiOperation(value = "物理删除评论")
    @DeleteMapping("/admin/comments")
    public Result deleteComments(@RequestBody List<Integer> commentIdList) {
        commentService.removeByIds(commentIdList);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

    @ApiOperation(value = "查询后台评论")
    @GetMapping("/admin/comments")
    public Result<PageDTO<CommentBackDTO>> listCommentBackDTO(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "查询成功", commentService.listCommentBackDTO(condition));
    }

}

