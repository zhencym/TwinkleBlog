package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.ArticlePreviewListDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.TagDTO;
import com.yuming.blog.entity.Tag;
import com.yuming.blog.service.ArticleService;
import com.yuming.blog.service.TagService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.Result;
import com.yuming.blog.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;



/**
 *
 * 标签
 */
@RestController
public class TagController {
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleService articleService;

    /**
     * 查看标签列表
     * @return
     */
    @GetMapping("/tags")
    public Result<PageDTO<TagDTO>> listTags() {
        return new Result<>(true, StatusConst.OK, "查询成功", tagService.listTags());
    }

    /**
     * 查看分类下对应的文章
     * @param tagId
     * @param current
     * @return
     */
    @GetMapping("/tags/{tagId}")
    public Result<ArticlePreviewListDTO> listArticlesByCategoryId(@PathVariable("tagId") Integer tagId, Integer current) {
        ConditionVO conditionVO = ConditionVO.builder()
                .tagId(tagId)
                .current(current)
                .build();
        return new Result<>(true, StatusConst.OK, "查询成功", articleService.listArticlesByCondition(conditionVO));
    }

    /**
     * 查看后台标签列表
     * @param condition
     * @return
     */
    @GetMapping("/admin/tags")
    public Result<PageDTO<Tag>> listTagBackDTO(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "查询成功", tagService.listTagBackDTO(condition));
    }

    /**
     * 添加或修改标签
     * @param tagVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.TAG, optDesc = "添加或修改标签")
    @PostMapping("/admin/tags")
    public Result saveOrUpdateTag(@Valid @RequestBody TagVO tagVO) {
        tagService.saveOrUpdateTag(tagVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 删除标签
     * @param tagIdList
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.TAG, optDesc = "删除标签")
    @DeleteMapping("/admin/tags")
    public Result deleteTag(@RequestBody List<Integer> tagIdList) {
        tagService.deleteTag(tagIdList);
        return new Result<>(true, StatusConst.OK, "删除成功");
    }

}

