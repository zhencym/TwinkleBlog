package com.yuming.blog.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuming.blog.dao.*;
import com.yuming.blog.dto.*;
import com.yuming.blog.dto.ArchiveDTO;
import com.yuming.blog.dto.ArticleBackDTO;
import com.yuming.blog.dto.ArticleOptionDTO;
import com.yuming.blog.dto.ArticlePaginationDTO;
import com.yuming.blog.dto.ArticlePreviewListDTO;
import com.yuming.blog.dto.ArticleRecommendDTO;
import com.yuming.blog.dto.CategoryBackDTO;
import com.yuming.blog.dto.TagDTO;
import com.yuming.blog.entity.Article;
import com.yuming.blog.entity.ArticleTag;
import com.yuming.blog.entity.Category;
import com.yuming.blog.entity.Tag;
import com.yuming.blog.service.ArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.service.ArticleTagService;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.utils.RedisLockUtils;
import com.yuming.blog.utils.UserUtil;
import com.yuming.blog.vo.ArticleVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.constant.RedisPrefixConst;
import com.yuming.blog.dao.ArticleDao;
import com.yuming.blog.dao.ArticleTagDao;
import com.yuming.blog.dao.CategoryDao;
import com.yuming.blog.dao.TagDao;
import com.yuming.blog.dto.ArticleDTO;
import com.yuming.blog.dto.ArticleHomeDTO;
import com.yuming.blog.dto.ArticlePreviewDTO;
import com.yuming.blog.dto.ArticleSearchDTO;
import com.yuming.blog.dto.PageDTO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, Article> implements ArticleService {
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private ArticleTagDao articleTagDao;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private HttpSession session;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisLockUtils redisLockUtils;
    // 注释掉，防止循环引用
//    @Autowired
//    private ArticleService articleService;
    @Autowired
    private ArticleTagService articleTagService;

    /**
     * 查询文章归档
     *
     * @param current 当前页码
     * @return 文章
     */
    @Override
    public PageDTO<ArchiveDTO> listArchives(Long current) {
        Page<Article> page = new Page<>(current, 10); //参数是页码和一页个数
        // 获取分页数据，Page是mybatisPlus的分页器，查询后得到分页结果，里面有7个属性，会使用PageDTO只取其中两种属性
        Page<Article> articlePage = articleDao.selectPage(page,
            new LambdaQueryWrapper<Article>() //构造sql查询语句，使用lambda语法、建造者模式构造包装成sql，获取id，标题，创建时间
                .select(Article::getId, Article::getArticleTitle, Article::getCreateTime)
                .orderByDesc(Article::getCreateTime)
                .eq(Article::getIsDelete, CommonConst.FALSE)
                .eq(Article::getIsDraft, CommonConst.FALSE));
        // 拷贝dto集合，把articlePage中List中的每一个Article对象变成ArchiveDTO对象
        List<ArchiveDTO> archiveDTOList = BeanCopyUtil.copyList(articlePage.getRecords(), ArchiveDTO.class);
        //返回PageDTO,去除了其他数据，只剩下列表和个数
        return new PageDTO<>(archiveDTOList, (int) articlePage.getTotal());
    }

    /**
     * 查询后台文章
     *
     * @param condition 条件
     * @return 文章列表
     */
    @Override
    public PageDTO<ArticleBackDTO> listArticleBackDTO(ConditionVO condition) {
        // 转换页码，其实就是把页码转换为limit a,b  中的a， 第a个开始返回，返回b个
        condition.setCurrent((condition.getCurrent() - 1) * condition.getSize());
        // 查询文章总量
        Integer count = articleDao.countArticleBacks(condition);
        if (count == 0) {
            return new PageDTO<>();
        }
        // 查询后台文章
        List<ArticleBackDTO> articleBackDTOList = articleDao.listArticleBacks(condition);
        // 查询文章点赞量和浏览量
        //查询不需要同步
        //boundHashOps绑定点赞量和浏览量的Map，当这个map修改时，redis中储存的值也相应修改。其余操作同opsForHash()
        Map<String, Integer> viewsCountMap = redisTemplate.boundHashOps(
            RedisPrefixConst.ARTICLE_VIEWS_COUNT).entries();
        Map<String, Integer> likeCountMap = redisTemplate.boundHashOps(
            RedisPrefixConst.ARTICLE_LIKE_COUNT).entries();
        // 封装点赞量和浏览量
        articleBackDTOList.forEach(item -> {
            //lambda表达式，根据文章的id作为key，找到对应的值，并set
            item.setViewsCount(Objects.requireNonNull(viewsCountMap).get(item.getId().toString()));
            item.setLikeCount(Objects.requireNonNull(likeCountMap).get(item.getId().toString()));
        });

        return new PageDTO<>(articleBackDTOList, count);
    }

    /**
     * 查询首页文章
     * @param current 当前页码
     * @return
     */
    @Override
    public List<ArticleHomeDTO> listArticles(Long current) {
        // 转换页码分页查询文章,一页10条
        List<ArticleHomeDTO> articleDTOList = articleDao.listArticles((current - 1) * 10);
        return articleDTOList;
    }

    @Override
    public ArticlePreviewListDTO listArticlesByCondition(ConditionVO condition) {
        // 转换页码
        condition.setCurrent((condition.getCurrent() - 1) * 9);
        // 搜索条件对应数据
        List<ArticlePreviewDTO> articlePreviewDTOList = articleDao.listArticlesByCondition(condition);
        // 搜索条件对应名(标签或分类名)
        String name;
        if (Objects.nonNull(condition.getCategoryId())) { // 存在分类id就往下查询，Objects.nonNul()判断对象是否为空，不空返回true
            name = categoryDao.selectOne(new LambdaQueryWrapper<Category>()  //封装一条sql语句
                    .select(Category::getCategoryName)  //利用lambda语法，获取类属性对应的数据库字段
                    .eq(Category::getId, condition.getCategoryId()))//一条sql语句至此
                    .getCategoryName();//从sql结果中获取CategoryName这一项
        } else {
            name = tagDao.selectOne(new LambdaQueryWrapper<Tag>() //否则根据标签id查询
                    .select(Tag::getTagName)
                    .eq(Tag::getId, condition.getTagId()))
                    .getTagName();
        }
        return ArticlePreviewListDTO.builder() //建造者模式，封装分类或标签名字，文章列表，返回
                .articlePreviewDTOList(articlePreviewDTOList)
                .name(name)
                .build();
    }

    @Override
    public ArticleDTO getArticleById(Integer articleId) throws InterruptedException {
        // 更新文章浏览量
        updateArticleViewsCount(articleId);
        // 查询id对应的文章
        ArticleDTO article = articleDao.getArticleById(articleId);
        // 查询上一篇下一篇文章
        Article lastArticle = articleDao.selectOne(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getArticleTitle, Article::getArticleCover) //得到上一篇的id和名字，略缩图
                .eq(Article::getIsDelete, CommonConst.FALSE)
                .eq(Article::getIsDraft, CommonConst.FALSE)
                .lt(Article::getId, articleId) //上一篇的id小，更久远之前的
                .orderByDesc(Article::getId)
                .last("limit 1")); //在sql尾部添加限制1个数据
        Article nextArticle = articleDao.selectOne(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getArticleTitle, Article::getArticleCover)//下一篇
                .eq(Article::getIsDelete, CommonConst.FALSE)
                .eq(Article::getIsDraft, CommonConst.FALSE)
                .gt(Article::getId, articleId) //下一篇的id大，更近期的
                .orderByAsc(Article::getId)
                .last("limit 1"));
        // 把Article类型的对象转换为ArticlePaginationDTO类型
        article.setLastArticle(BeanCopyUtil.copyObject(lastArticle, ArticlePaginationDTO.class));
        article.setNextArticle(BeanCopyUtil.copyObject(nextArticle, ArticlePaginationDTO.class));
        // 查询相关推荐文章
        article.setArticleRecommendList(articleDao.listArticleRecommends(articleId));
        // 封装点赞量和浏览量
        // 查询不需要同步
        article.setViewsCount((Integer) redisTemplate.boundHashOps(
            RedisPrefixConst.ARTICLE_VIEWS_COUNT).get(articleId.toString()));
        article.setLikeCount((Integer) redisTemplate.boundHashOps(
            RedisPrefixConst.ARTICLE_LIKE_COUNT).get(articleId.toString()));
        return article;
    }

    @Override
    public List<ArticleRecommendDTO> listNewestArticles() {
        // 查询最新文章
        List<Article> articleList = articleDao.selectList(new LambdaQueryWrapper<Article>()//最新文章，id、标题、略缩图、创建时间
                .select(Article::getId, Article::getArticleTitle, Article::getArticleCover, Article::getCreateTime)
                .eq(Article::getIsDelete, CommonConst.FALSE)
                .eq(Article::getIsDraft, CommonConst.FALSE)
                .orderByDesc(Article::getId)
                .last("limit 5"));
        return BeanCopyUtil.copyList(articleList, ArticleRecommendDTO.class);
    }

    /**
     * 更新文章浏览量
     *
     * @param articleId 文章id
     */
    @Async //异步调用
    public void updateArticleViewsCount(Integer articleId) throws InterruptedException {
        // 判断是否第一次访问，增加浏览量
        // 用户在一个会话session期间，只能增加一次文章访问量。
        // session记录用户在此session期间访问过的文章id
        // 修改操作需要同步，使用分布式锁
        String value = IdUtil.randomUUID(); //线程标识
        //redis尝试获取锁，加锁
        Boolean getLock = this.redisLockUtils.getLock(
            RedisPrefixConst.ARTICLE_VIEWS_COUNT+ RedisPrefixConst.LOCK,value);
        if (getLock) {
        Set<Integer> set = (Set<Integer>) session.getAttribute("articleSet");
        if (Objects.isNull(set)) {
            set = new HashSet<>();
        }
        if (!set.contains(articleId)) {
            set.add(articleId);
            session.setAttribute("articleSet", set);
            // 浏览量+1
            redisTemplate.boundHashOps(RedisPrefixConst.ARTICLE_VIEWS_COUNT).increment(articleId.toString(), 1);
            }
        //释放分布式锁
        this.redisLockUtils.releaseLock(RedisPrefixConst.ARTICLE_VIEWS_COUNT+ RedisPrefixConst.LOCK,value);
        //没有获取到锁
        } else {
            //线程休眠 然后尝试递归获取锁
            Thread.sleep(RedisLockUtils.LOCK_REDIS_WAIT);
            this.updateArticleViewsCount(articleId);
        }
    }

    @Override
    public ArticleOptionDTO listArticleOptionDTO() {
        // 查询文章分类选项
        List<Category> categoryList = categoryDao.selectList(new LambdaQueryWrapper<Category>()
                .select(Category::getId, Category::getCategoryName));
        // Category转化为CategoryBackDTO
        List<CategoryBackDTO> categoryDTOList = BeanCopyUtil.copyList(categoryList, CategoryBackDTO.class);
        // 查询文章标签选项
        List<Tag> tagList = tagDao.selectList(new LambdaQueryWrapper<Tag>()
                .select(Tag::getId, Tag::getTagName));
        //转化
        List<TagDTO> tagDTOList = BeanCopyUtil.copyList(tagList, TagDTO.class);
        //构建
        return ArticleOptionDTO.builder()
                .categoryDTOList(categoryDTOList)
                .tagDTOList(tagDTOList)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveArticleLike(Integer articleId) throws InterruptedException {
        //使用分布式锁
        String value = IdUtil.randomUUID();
        Boolean getLock = this.redisLockUtils.getLock(
            RedisPrefixConst.ARTICLE_USER_LIKE+ RedisPrefixConst.ARTICLE_LIKE_COUNT+ RedisPrefixConst.LOCK,value);
        if (getLock) {
            // 查询当前用户点赞过的文章id集合
            Set<Integer> articleLikeSet = (Set<Integer>) redisTemplate.boundHashOps(
                RedisPrefixConst.ARTICLE_USER_LIKE).get(UserUtil.getLoginUser().getUserInfoId().toString());
            // 第一次点赞则创建
            if (CollectionUtils.isEmpty(articleLikeSet)) { //CollectionUtils.isEmpty() 判断集合为null或size=0
                articleLikeSet = new HashSet<>();
                //Objects.nonNull(); 这个是只判断obj不为null即返回true
            }
            // 判断是否点赞
            if (articleLikeSet.contains(articleId)) {
                // 点过赞则删除文章id
                articleLikeSet.remove(articleId);
                // 文章点赞量-1
                redisTemplate.boundHashOps(RedisPrefixConst.ARTICLE_LIKE_COUNT).increment(articleId.toString(), -1);
            } else {
                // 未点赞则增加文章id
                articleLikeSet.add(articleId);
                // 文章点赞量+1
                redisTemplate.boundHashOps(RedisPrefixConst.ARTICLE_LIKE_COUNT).increment(articleId.toString(), 1);
            }
            // 保存点赞记录，更新了redis数据
            redisTemplate.boundHashOps(RedisPrefixConst.ARTICLE_USER_LIKE).put(UserUtil.getLoginUser().getUserInfoId().toString(), articleLikeSet);
            //释放分布式锁
            this.redisLockUtils.releaseLock(
                RedisPrefixConst.ARTICLE_USER_LIKE+ RedisPrefixConst.ARTICLE_LIKE_COUNT+ RedisPrefixConst.LOCK,value);
        } else {
            //重试
            Thread.sleep(RedisLockUtils.LOCK_REDIS_WAIT);
            this.saveArticleLike(articleId);
        }
    }

    @Transactional(rollbackFor = Exception.class)  //基类异常就回滚，可以说有异常就回滚了
    @Override
    public void saveOrUpdateArticle(ArticleVO articleVO) {
        // 保存或修改文章
        Article article = Article.builder()
                .id(articleVO.getId())
                .userId(UserUtil.getLoginUser().getUserInfoId())
                .categoryId(articleVO.getCategoryId())
                .articleCover(articleVO.getArticleCover())
                .articleTitle(articleVO.getArticleTitle())
                .articleContent(articleVO.getArticleContent())
                .createTime(Objects.isNull(articleVO.getId()) ? new Date() : null) //文章没有id，说明是新文章，给个创建日期
                .updateTime(Objects.nonNull(articleVO.getId()) ? new Date() : null) //文章有id，说明是更新文章，给个更新日期
                .isTop(articleVO.getIsTop())
                .isDraft(articleVO.getIsDraft())
                .build();
        this.saveOrUpdate(article);
        // 编辑文章则删除文章所有旧标签
        if (Objects.nonNull(articleVO.getId()) && articleVO.getIsDraft().equals(CommonConst.FALSE)) {
            articleTagDao.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleVO.getId()));
        }
        // 添加文章新标签
        if (!articleVO.getTagIdList().isEmpty()) {
            // getTagIdList().stream().map列表变成流，list里的每个tagID映射成一个ArticleTag对象
            List<ArticleTag> articleTagList = articleVO.getTagIdList().stream().map(tagId -> ArticleTag.builder()
                    .articleId(article.getId())
                    .tagId(tagId)
                    .build()) //建造一个ArticleTag对象
                    .collect(Collectors.toList()); //多个对象变成list

            articleTagService.saveBatch(articleTagList); //保存列表
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateArticleTop(Integer articleId, Integer isTop) {
        // 修改文章置顶状态
        Article article = Article.builder()
                .id(articleId)
                .isTop(isTop)
                .build();
        articleDao.updateById(article);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateArticleDelete(DeleteVO deleteVO) {
        // 修改文章逻辑删除状态
        List<Article> articleList = deleteVO.getIdList().stream().map(id -> Article.builder() //每个id映射成一个Article对象
                .id(id)
                .isTop(CommonConst.FALSE)
                .isDelete(deleteVO.getIsDelete())
                .build())
                .collect(Collectors.toList());
        this.updateBatchById(articleList);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteArticles(List<Integer> articleIdList) {
        // 删除文章标签关联
        articleTagDao.delete(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIdList));
        // 删除文章
        articleDao.deleteBatchIds(articleIdList);
    }

    /**
     * 使用elastsearch查询文章
     * @param condition 条件
     * @return
     */
    @Override
    public List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition) {
        return searchArticle(buildQuery(condition));
    }

    /**
     * mysql搜索文章标题或内容返回文章
     */
    public List<ArticleSearchDTO> listArticleByTitleContent(ConditionVO condition) {
        return articleDao.listArticlesByTitleContent(condition);
    }

    @Override
    public ArticleVO getArticleBackById(Integer articleId) {
        // 查询文章信息
        Article article = articleDao.selectOne(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getArticleTitle, Article::getArticleContent, Article::getArticleCover, Article::getCategoryId, Article::getIsTop, Article::getIsDraft)
                .eq(Article::getId, articleId));
        // 查询文章标签
        List<Integer> tagIdList = articleTagDao.selectList(new LambdaQueryWrapper<ArticleTag>()
                .select(ArticleTag::getTagId)
                .eq(ArticleTag::getArticleId, article.getId()))
                .stream()
                .map(ArticleTag::getTagId).collect(Collectors.toList());
        return ArticleVO.builder()
                .id(article.getId())
                .articleTitle(article.getArticleTitle())
                .articleContent(article.getArticleContent())
                .articleCover(article.getArticleCover())
                .categoryId(article.getCategoryId())
                .isTop(article.getIsTop())
                .tagIdList(tagIdList)
                .isDraft(article.getIsDraft())
                .build();
    }

    /**
     * 搜索文章构造
     *
     * @param condition 条件
     * @return es条件构造器
     */
    private NativeSearchQueryBuilder buildQuery(ConditionVO condition) {
        // 条件构造器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 根据关键词搜索文章标题或内容
        if (Objects.nonNull(condition.getKeywords())) {
            boolQueryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("articleTitle", condition.getKeywords()))
                    .should(QueryBuilders.matchQuery("articleContent", condition.getKeywords())))
                    .must(QueryBuilders.termQuery("isDelete", CommonConst.FALSE));
        }
        // 查询
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 文章搜索结果高亮
     *
     * @param nativeSearchQueryBuilder es条件构造器
     * @return 搜索结果
     */
    private List<ArticleSearchDTO> searchArticle(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        // 添加文章标题高亮
        HighlightBuilder.Field titleField = new HighlightBuilder.Field("articleTitle");
        titleField.preTags("<span style='color:#f47466'>");
        titleField.postTags("</span>");
        // 添加文章内容高亮
        HighlightBuilder.Field contentField = new HighlightBuilder.Field("articleContent");
        contentField.preTags("<span style='color:#f47466'>");
        contentField.postTags("</span>");
        contentField.fragmentSize(200);
        nativeSearchQueryBuilder.withHighlightFields(titleField, contentField);
        // 搜索
        SearchHits<ArticleSearchDTO> search = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), ArticleSearchDTO.class);
        return search.getSearchHits().stream().map(hit -> {
            ArticleSearchDTO article = hit.getContent();
            // 获取文章标题高亮数据
            List<String> titleHighLightList = hit.getHighlightFields().get("articleTitle");
            if (CollectionUtils.isNotEmpty(titleHighLightList)) {
                // 替换标题数据
                article.setArticleTitle(titleHighLightList.get(0));
            }
            // 获取文章内容高亮数据
            List<String> contentHighLightList = hit.getHighlightFields().get("articleContent");
            if (CollectionUtils.isNotEmpty(contentHighLightList)) {
                // 替换内容数据
                article.setArticleContent(contentHighLightList.get(0));
            }
            return article;
        }).collect(Collectors.toList());
    }



}
