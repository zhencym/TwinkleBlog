package com.yuming.blog.dao;

import com.yuming.blog.dto.*;
import com.yuming.blog.dto.ArticleBackDTO;
import com.yuming.blog.dto.ArticleRecommendDTO;
import com.yuming.blog.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.dto.ArticleDTO;
import com.yuming.blog.dto.ArticleHomeDTO;
import com.yuming.blog.dto.ArticlePreviewDTO;
import com.yuming.blog.dto.ArticleSearchDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ArticleDao extends BaseMapper<Article> {

    /**
     * 查询首页文章
     *
     * @param current 当前页码，实际上没有用到页码
     * @return 首页文章集合
     */
    List<ArticleHomeDTO> listArticles(Long current);

    /**
     * 根据id查询文章
     *
     * @param articleId 文章id
     * @return 文章
     */
    ArticleDTO getArticleById(Integer articleId);

    /**
     * 根据文章标题，内容查询文章，elastsearch的方法
     */
    List<ArticleSearchDTO> listArticlesByTitleContent(@Param("condition") ConditionVO condition);

    /**
     * 根据条件查询文章，其实具体是根据当前页码、分类id和标签id查找文章（如果存在这个条件）
     *
     * @param condition 条件
     * @return 文章集合
     */
    List<ArticlePreviewDTO> listArticlesByCondition(@Param("condition") ConditionVO condition);

    /**
     * 查询后台文章列表，
     * 这是后台文章列表整个页面的功能的一条sql实现。实现删除、草稿、标题模糊查询、返回数目的查询
     * 传入参数实际上是，是否删除、草稿、查询关键字、当前页和返回size
     *
     * @param condition 条件
     * @return 后台文章集合
     */
    List<ArticleBackDTO> listArticleBacks(@Param("condition") ConditionVO condition);

    /**
     * 查询后台文章总量
     * 统计删除、草稿、标题关键字文章的总数量，参数就是删除、草稿、标题关键字
     *
     * @param condition 条件
     * @return 文章总量
     */
    Integer countArticleBacks(@Param("condition") ConditionVO condition);

    /**
     * 查询文章排行
     * 文章id首先得在articleIdList里
     * 然后用field是自定义排序，按照id字段，后面自定义的顺序排序，其实也就是articleIdList里面的id的顺序
     * @param articleIdList
     * @return
     */
    List<Article> listArticleRank(@Param("articleIdList") List<Integer> articleIdList);

    /**
     * 查看文章的推荐文章
     * 这里是根据 根据文章的标签id相同 来推荐其他文章，
     * 先根据文章id得到 标签id，根据标签id相同，得到其他文章id，去重，最终获取文章内容。
     * @param articleId 文章id
     * @return 推荐文章
     */
    List<ArticleRecommendDTO> listArticleRecommends(@Param("articleId") Integer articleId);

}
