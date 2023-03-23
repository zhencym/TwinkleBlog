package com.yuming.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.dao.*;
import com.yuming.blog.dto.*;
import com.yuming.blog.dto.BlogBackInfoDTO;
import com.yuming.blog.dto.CategoryDTO;
import com.yuming.blog.dto.UniqueViewDTO;
import com.yuming.blog.entity.Article;
import com.yuming.blog.entity.UserInfo;
import com.yuming.blog.service.BlogInfoService;
import com.yuming.blog.service.UniqueViewService;
import com.yuming.blog.constant.RedisPrefixConst;
import com.yuming.blog.dao.ArticleDao;
import com.yuming.blog.dao.CategoryDao;
import com.yuming.blog.dao.MessageDao;
import com.yuming.blog.dao.TagDao;
import com.yuming.blog.dao.UserInfoDao;
import com.yuming.blog.dto.ArticleRankDTO;
import com.yuming.blog.dto.BlogHomeInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class BlogInfoServiceImpl implements BlogInfoService {
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private UniqueViewService uniqueViewService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public BlogHomeInfoDTO getBlogInfo() {
        //查询博主信息(头像、名字、介绍)
        UserInfo userInfo = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfo>()
                .select(UserInfo::getAvatar, UserInfo::getNickname, UserInfo::getIntro)
                .eq(UserInfo::getId, CommonConst.BLOGGER_ID));
        // 查询文章数量
        // 因为文章都是自己写的，所以统计所有即可。不需要筛选账号
        Integer articleCount = articleDao.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDraft, CommonConst.FALSE)
                .eq(Article::getIsDelete, CommonConst.FALSE));
        // 查询分类数量
        Integer categoryCount = categoryDao.selectCount(null);
        // 查询标签数量
        Integer tagCount = tagDao.selectCount(null);
        // 查询公告
        Object value = redisTemplate.boundValueOps(RedisPrefixConst.NOTICE).get();
        String notice = Objects.nonNull(value) ? value.toString() : "发布你的第一篇公告吧";
        // 查询访问量
        String viewsCount = Objects.requireNonNull(redisTemplate.boundValueOps(
            RedisPrefixConst.BLOG_VIEWS_COUNT).get()).toString();//需要非空，否则抛异常
        // 封装数据
        //System.out.println(userInfo);
        return BlogHomeInfoDTO.builder()
                .nickname(userInfo.getNickname())
                .avatar(userInfo.getAvatar())
                .intro(userInfo.getIntro())
                .articleCount(articleCount)
                .categoryCount(categoryCount)
                .tagCount(tagCount)
                .notice(notice)
                .viewsCount(viewsCount)
                .build();
    }

    @Override
    public BlogBackInfoDTO getBlogBackInfo() {
        // 查询访问量
        Integer viewsCount = (Integer) redisTemplate.boundValueOps(RedisPrefixConst.BLOG_VIEWS_COUNT).get();
        // 查询留言量
        Integer messageCount = messageDao.selectCount(null);
        // 查询用户量
        Integer userCount = userInfoDao.selectCount(null);
        // 查询文章量
        Integer articleCount = articleDao.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, CommonConst.FALSE)
                .eq(Article::getIsDraft, CommonConst.FALSE));
        // 查询一周用户量
        List<UniqueViewDTO> uniqueViewList = uniqueViewService.listUniqueViews();
        // 查询分类数据
        List<CategoryDTO> categoryDTOList = categoryDao.listCategoryDTO();
        // 查询redis访问量前五的文章
        Map<String, Integer> articleViewsMap = redisTemplate.boundHashOps(
            RedisPrefixConst.ARTICLE_VIEWS_COUNT).entries();
        // 将文章进行倒序排序
        // 关键在于从redis取出键对值后，找出value前五的文章id，排序算法
        List<Integer> articleIdList = Objects.requireNonNull(articleViewsMap).entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //根据键值逆序排序
                .map(item -> Integer.valueOf(item.getKey()))//map映射，把set里的每个键值对，取key，变成整数
                .collect(Collectors.toList());
        // 提取前五篇文章
        int index = Math.min(articleIdList.size(), 5); //不足5篇就取size篇
        articleIdList = articleIdList.subList(0, index);
        // 文章为空直接返回
        if (articleIdList.isEmpty()) {
            return BlogBackInfoDTO.builder()
                    .viewsCount(viewsCount)
                    .messageCount(messageCount)
                    .userCount(userCount)
                    .articleCount(articleCount)
                    .categoryDTOList(categoryDTOList)
                    .uniqueViewDTOList(uniqueViewList)
                    .build();
        }
        // 查询文章标题
        List<Article> articleList = articleDao.listArticleRank(articleIdList);
        // 封装浏览量
        List<ArticleRankDTO> articleRankDTOList = articleList.stream().map(article -> ArticleRankDTO.builder()
                .articleTitle(article.getArticleTitle())
                .viewsCount(articleViewsMap.get(article.getId().toString()))
                .build())
                .collect(Collectors.toList());
        return BlogBackInfoDTO.builder()
                .viewsCount(viewsCount) //访问量
                .messageCount(messageCount) //留言量
                .userCount(userCount) //用户量
                .articleCount(articleCount) //文章量
                .categoryDTOList(categoryDTOList) //分类
                .uniqueViewDTOList(uniqueViewList) //一周访问量
                .articleRankDTOList(articleRankDTOList) //文章访问量前五
                .build();
    }

    @Override
    public String getAbout() {
        Object value = redisTemplate.boundValueOps(RedisPrefixConst.ABOUT).get();
        return Objects.nonNull(value) ? value.toString() : "这个博主比较懒，还没有写个人介绍...";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAbout(String aboutContent) {
        redisTemplate.boundValueOps(RedisPrefixConst.ABOUT).set(aboutContent);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateNotice(String notice) {
        redisTemplate.boundValueOps(RedisPrefixConst.NOTICE).set(notice);
    }

    @Override
    public String getNotice() {
        Object value = redisTemplate.boundValueOps(RedisPrefixConst.NOTICE).get();
        return Objects.nonNull(value) ? value.toString() : "发布你的第一篇公告吧";
    }

}
