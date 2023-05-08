package com.yuming.blog.constant;

import com.yuming.blog.dto.ArticleOptionDTO;

/**
 * redis常量
 *
 **/
public class RedisPrefixConst {

    /**
     * redis分布式锁 后缀
     */
    public static final String LOCK = "_lock";

    /**
     * 验证码过期时间
     */
    public static final long CODE_EXPIRE_TIME = 15 * 60 * 1000; // 15分钟

    /**
     * 缓存时长10min
     */
    public static final long MINUTE_10 = 10;

    /**
     * 缓存时长1小时
     */
    public static final long HOURS_1 = 1;

    /**
     * 缓存时长2小时
     */
    public static final long HOURS_2 = 2;

    /**
     * 用户在线时间
     */
    public static final long LOGIN_EXPIRE_TIME = 6; // 6小时

    /**
     * 验证码
     */
    public static final String CODE_KEY = "code_"; // 保存验证码

    /**
     * 博客浏览量
     */
    public static final String BLOG_VIEWS_COUNT = "blog_views_count";

    /**
     * 文章浏览量
     */
    public static final String ARTICLE_VIEWS_COUNT = "article_views_count";

    /**
     * 文章点赞量
     */
    public static final String ARTICLE_LIKE_COUNT = "article_like_count"; //文章id为key，点赞数量为value

    /**
     * 用户点赞文章
     */
    public static final String ARTICLE_USER_LIKE = "article_user_like"; //用户id为key，点赞过的文章id的list组成value

    /**
     * 评论点赞量
     */
    public static final String COMMENT_LIKE_COUNT = "comment_like_count";

    /**
     * 用户点赞评论
     */
    public static final String COMMENT_USER_LIKE = "comment_user_like";//用户id为key，点赞过的评论id为value

    /**
     * 关于我信息
     */
    public static final String ABOUT = "about";

    /**
     * 公告
     */
    public static final String NOTICE = "notice";

    /**
     * ip集合
     */
    public static final String IP_SET = "ip_set";

    /**
     * 用户登录记录
     */
    public static final String LOGIN = "login_";

    /**
     * 首页文章列表缓存
     */
    public static final String LITST_ArticleHomeDTO = "list_articleHomeDTO_";


    /**
     * 文章归档缓存
     */
    public static final String ArchiveDTO = "ArchiveDTO_";

    /**
     * 文章标签缓存
     */
    public static final String ArticleOptionDTO = "ArticleOptionDTO_";

}
