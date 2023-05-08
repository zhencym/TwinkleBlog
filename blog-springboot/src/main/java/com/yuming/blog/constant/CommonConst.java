package com.yuming.blog.constant;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

/**
 * 公共常量
 *
 **/
public class CommonConst {

    /**
     * 否
     */
    public static final int FALSE = 0;

    /**
     * 是
     */
    public static final int TURE = 1;

    /**
     * 博主id
     */
    public static final int BLOGGER_ID = 1;

    /**
     * 默认用户昵称
     */
    public static final String DEFAULT_NICKNAME = "用户" + IdWorker.getId(); //生成唯一ID

    /**
     * 默认用户头像
     */
    public static final String DEFAULT_AVATAR = "https://api.vvhan.com/api/acgimg ";

    /**
     * 前端组件名
     */
    public static String COMPONENT = "Layout";

    /**
     * 网站域名
     */
    public static final String URL = "https://localhost:8666/";

    /**
     * 文章页面路径
     */
    public static final String ARTICLE_PATH = "/articles/";

    /**
     * 友联页面路径
     */
    public static final String LINK_PATH = "/links";

}
