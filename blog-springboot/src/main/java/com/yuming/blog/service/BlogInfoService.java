package com.yuming.blog.service;

import com.yuming.blog.dto.BlogBackInfoDTO;
import com.yuming.blog.dto.BlogHomeInfoDTO;

/**
 * 管理员端，没必要缓存，访问量不大
 */

public interface BlogInfoService  {

    /**
     * 获取前台首页数据
     * 博客首页的博主信息
     * @return 博客首页信息
     */
    BlogHomeInfoDTO getBlogInfo();

    /**
     * 获取后台首页数据
     * @return 博客后台信息
     */
    BlogBackInfoDTO getBlogBackInfo();

    /**
     * 获取关于我内容
     * @return 关于我内容
     */
    String getAbout();

    /**
     * 修改关于我内容
     * @param aboutContent 关于我内容
     */
    void updateAbout(String aboutContent);

    /**
     * 修改公告
     * @param notice 公告
     */
    void updateNotice(String notice);

    /**
     * 后台查看公告
     * @return 公告
     */
    String getNotice();

}
