package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.*;
import com.yuming.blog.dto.ArchiveDTO;
import com.yuming.blog.dto.ArticleBackDTO;
import com.yuming.blog.dto.ArticleOptionDTO;
import com.yuming.blog.dto.ArticleRecommendDTO;
import com.yuming.blog.enums.FilePathEnum;
import com.yuming.blog.service.ArticleService;
import com.yuming.blog.utils.OSSUtil;
import com.yuming.blog.vo.*;
import com.yuming.blog.dto.ArticleDTO;
import com.yuming.blog.dto.ArticleHomeDTO;
import com.yuming.blog.dto.ArticleSearchDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.vo.ArticleVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;
import com.yuming.blog.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;

import static com.yuming.blog.constant.OptTypeConst.*;

/**
 * 文章
 */
@Api(tags = "文章模块")
@RestController
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @ApiOperation(value = "查看文章归档")
    @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Long")
    @GetMapping("/articles/archives")
    public Result<PageDTO<ArchiveDTO>> listArchives(Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArchives(current));
    }

    @ApiOperation(value = "查看首页文章")
    @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Long")
    @GetMapping("/articles")
    public Result<List<ArticleHomeDTO>> listArticles(Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticles(current));
    }

    @ApiOperation(value = "查看后台文章")
    @GetMapping("/admin/articles")
    public Result<PageDTO<ArticleBackDTO>> listArticleBackDTO(ConditionVO conditionVO) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticleBackDTO(conditionVO));
    }

    @ApiOperation(value = "查看文章选项")
    @GetMapping("/admin/articles/options")
    public Result<ArticleOptionDTO> listArticleOptionDTO() {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticleOptionDTO());
    }

    @OptLog(optType = SAVE_OR_UPDATE)
    @ApiOperation(value = "添加或修改文章")
    @PostMapping("/admin/articles")
    public Result saveArticle(@Valid @RequestBody ArticleVO articleVO) {
        articleService.saveOrUpdateArticle(articleVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改文章置顶")
    @PutMapping("/admin/articles/top/{articleId}")
    public Result updateArticleTop(@PathVariable("articleId") Integer articleId, Integer isTop) {
        articleService.updateArticleTop(articleId, isTop);
        return new Result<>(true, StatusConst.OK, "修改成功");
    }

    @ApiOperation(value = "上传文章图片")
    @ApiImplicitParam(name = "file", value = "文章图片", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/articles/images")
    public Result<String> saveArticleImages(MultipartFile file) {
        return new Result<>(true, StatusConst.OK, "上传成功", OSSUtil.upload(file, FilePathEnum.ARTICLE.getPath()));
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "恢复或删除文章")
    @PutMapping("/admin/articles")
    public Result updateArticleDelete(DeleteVO deleteVO) {
        articleService.updateArticleDelete(deleteVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    @OptLog(optType = REMOVE)
    @ApiOperation(value = "物理删除文章")
    @DeleteMapping("/admin/articles")
    public Result deleteArticles(@RequestBody List<Integer> articleIdList) {
        articleService.deleteArticles(articleIdList);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

    @ApiOperation(value = "根据id查看后台文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @GetMapping("/admin/articles/{articleId}")
    public Result<ArticleVO> getArticleBackById(@PathVariable("articleId") Integer articleId) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.getArticleBackById(articleId));
    }

    @ApiOperation(value = "根据id查看文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @GetMapping("/articles/{articleId}")
    public Result<ArticleDTO> getArticleById(@PathVariable("articleId") Integer articleId)
        throws InterruptedException {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.getArticleById(articleId));
    }

    @ApiOperation(value = "查看最新文章")
    @GetMapping("/articles/newest")
    public Result<List<ArticleRecommendDTO>> listNewestArticles() {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listNewestArticles());
    }

    @ApiOperation(value = "搜索文章")
    @GetMapping("/articles/search")
    public Result<List<ArticleSearchDTO>> listArticlesBySearch( ConditionVO condition) {
        System.out.println(condition);
        //原本是elastsearch搜索
        //return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticlesBySearch(condition));
        //替换成mysql搜索,
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticleByTitleContent(condition));
    }

    @ApiOperation(value = "点赞文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @PostMapping("/articles/like")
    public Result saveArticleLike(Integer articleId) throws InterruptedException {
        articleService.saveArticleLike(articleId);
        return new Result<>(true, StatusConst.OK, "点赞成功");
    }

}

