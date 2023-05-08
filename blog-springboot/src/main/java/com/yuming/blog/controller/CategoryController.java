package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.*;
import com.yuming.blog.dto.ArticlePreviewListDTO;
import com.yuming.blog.dto.CategoryDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.entity.Category;
import com.yuming.blog.service.ArticleService;
import com.yuming.blog.service.CategoryService;
import com.yuming.blog.vo.CategoryVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.Result;
import com.yuming.blog.constant.StatusConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 分类
 */
@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleService articleService;

    /**
     * 查看分类列表
     * @return
     */
    @GetMapping("/categories")
    public Result<PageDTO<CategoryDTO>> listCategories() {
        return new Result<>(true, StatusConst.OK, "查询成功", categoryService.listCategories());
    }

    /**
     * 查看后台分类列表
     * @param condition
     * @return
     */
    @GetMapping("/admin/categories")
    public Result<PageDTO<Category>> listCategoryBackDTO(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "查询成功", categoryService.listCategoryBackDTO(condition));
    }

    /**
     *添加或修改分类
     * @param categoryVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.CLASSFY, optDesc = "添加或修改分类")
    @PostMapping("/admin/categories")
    public Result saveOrUpdateCategory(@Valid @RequestBody CategoryVO categoryVO) {
        categoryService.saveOrUpdateCategory(categoryVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 删除分类
     * @param categoryIdList
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.CLASSFY, optDesc = "删除分类")
    @DeleteMapping("/admin/categories")
    public Result deleteCategories(@RequestBody List<Integer> categoryIdList) {
        categoryService.deleteCategory(categoryIdList);
        return new Result<>(true, StatusConst.OK, "删除成功");
    }

    /**
     * 查看分类下对应的文章
     * @param categoryId
     * @param current
     * @return
     */
    @OptLog(optType = OptLogType.VIEW, optModule = OptLogModule.CLASSFY, optDesc = "查看分类下对应的文章")
    @GetMapping("/categories/{categoryId}")
    public Result<ArticlePreviewListDTO> listArticlesByCategoryId(@PathVariable("categoryId") Integer categoryId, Integer current) {
        ConditionVO conditionVO = ConditionVO.builder()
                .categoryId(categoryId)
                .current(current)
                .build();
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticlesByCondition(conditionVO));
    }

}

