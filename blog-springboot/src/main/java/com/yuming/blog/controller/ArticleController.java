package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.ArchiveDTO;
import com.yuming.blog.dto.ArticleBackDTO;
import com.yuming.blog.dto.ArticleOptionDTO;
import com.yuming.blog.dto.ArticleRecommendDTO;
import com.yuming.blog.enums.FilePathEnum;
import com.yuming.blog.service.ArticleService;
import com.yuming.blog.utils.OSSUtil;
import com.yuming.blog.dto.ArticleDTO;
import com.yuming.blog.dto.ArticleHomeDTO;
import com.yuming.blog.dto.ArticleSearchDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.vo.ArticleVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;
import com.yuming.blog.vo.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;


/**
 * 文章
 */
@RestController
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 查看归档文章
     * @param current
     * @param h
     * @return
     */
    @GetMapping("/articles/archives")
    public Result<PageDTO<ArchiveDTO>> listArchives(Long current, HttpServletRequest h) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArchives(current));

    }

    /**
     * 查看文章首页
     * @param current
     * @return
     */
    @GetMapping("/articles")
    public Result<List<ArticleHomeDTO>> listArticles(Long current) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticles(current));
    }

    /**
     * 查看后台文章
     * @param conditionVO
     * @return
     */
    @GetMapping("/admin/articles")
    public Result<PageDTO<ArticleBackDTO>> listArticleBackDTO(ConditionVO conditionVO) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticleBackDTO(conditionVO));
    }

    /**
     * 查看文章选项
     * @return
     */
    @GetMapping("/admin/articles/options")
    public Result<ArticleOptionDTO> listArticleOptionDTO() {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticleOptionDTO());
    }

    /**
     * 添加或修改文章
     * @param articleVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.ARTICLE, optDesc = "添加或修改文章")
    @PostMapping("/admin/articles")
    public Result saveArticle(@Valid @RequestBody ArticleVO articleVO) {
        articleService.saveOrUpdateArticle(articleVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 置顶文章
     * @param articleId
     * @param isTop
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.ARTICLE, optDesc = "修改文章置顶")
    @PutMapping("/admin/articles/top/{articleId}")
    public Result updateArticleTop(@PathVariable("articleId") Integer articleId, Integer isTop) {
        articleService.updateArticleTop(articleId, isTop);
        return new Result<>(true, StatusConst.OK, "修改成功");
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @PostMapping("/admin/articles/images")
    public Result<String> saveArticleImages(MultipartFile file) {
        return new Result<>(true, StatusConst.OK, "上传成功", OSSUtil.upload(file, FilePathEnum.ARTICLE.getPath()));
    }

    /**
     * 恢复或删除文章
     * @param deleteVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.ARTICLE, optDesc = "恢复或删除文章")
    @PutMapping("/admin/articles")
    public Result updateArticleDelete(DeleteVO deleteVO) {
        articleService.updateArticleDelete(deleteVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 物理删除文章
     * @param articleIdList
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.ARTICLE, optDesc = "物理删除文章")
    @DeleteMapping("/admin/articles")
    public Result deleteArticles(@RequestBody List<Integer> articleIdList) {
        articleService.deleteArticles(articleIdList);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

    /**
     * 根据id查看后台文章
     * @param articleId
     * @return
     */
    @GetMapping("/admin/articles/{articleId}")
    public Result<ArticleVO> getArticleBackById(@PathVariable("articleId") Integer articleId) {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.getArticleBackById(articleId));
    }

    /**
     * 根据id查看文章
     * @param articleId
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/articles/{articleId}")
    public Result<ArticleDTO> getArticleById(@PathVariable("articleId") Integer articleId)
        throws InterruptedException {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.getArticleById(articleId));
    }

    /**
     * 查看最新文章
     * @return
     */
    @GetMapping("/articles/newest")
    public Result<List<ArticleRecommendDTO>> listNewestArticles() {
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listNewestArticles());
    }

    /**
     * 搜索文章
     * @param condition
     * @return
     */
    @GetMapping("/articles/search")
    public Result<List<ArticleSearchDTO>> listArticlesBySearch( ConditionVO condition) {
        System.out.println(condition);
        //mysql模糊搜索
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticleByTitleContent(condition));
    }

    /**
     * 点赞文章
     * @param articleId
     * @return
     * @throws InterruptedException
     */
    @PostMapping("/articles/like")
    public Result saveArticleLike(Integer articleId) throws InterruptedException {
        articleService.saveArticleLike(articleId);
        return new Result<>(true, StatusConst.OK, "点赞成功");
    }

}

