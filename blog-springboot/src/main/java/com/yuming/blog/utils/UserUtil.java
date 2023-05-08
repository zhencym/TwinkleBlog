package com.yuming.blog.utils;

import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.entity.UserAuth;
import com.yuming.blog.entity.UserInfo;
import com.yuming.blog.exception.UnLoginException;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 用户工具类
 *
 */
@Component
public class UserUtil {



    /**
     * 获取当前登录用户
     * 每一个登录用户都有一个session
     * @return 用户登录信息
     */
    public static UserInfoDTO getLoginUser(HttpServletRequest request) {
        UserInfoDTO userInfoDTO = (UserInfoDTO) request.getSession().getAttribute("userInfoDTO");
        if (userInfoDTO == null) {
            throw new UnLoginException("请先登录！");
        }
        return userInfoDTO;
    }

    /**
     * 封装用户登录信息
     *
     * @param user           用户账号
     * @param userInfo       用户信息
     * @param articleLikeSet 点赞文章id集合
     * @param commentLikeSet 点赞评论id集合
     * @param request        请求
     * @return 用户登录信息
     */
    public static UserInfoDTO convertLoginUser(UserAuth user, UserInfo userInfo, List<String> roleList, Set<Integer> articleLikeSet, Set<Integer> commentLikeSet, HttpServletRequest request) {
        // 获取登录信息
        String ipAddr = IpUtil.getIpAddr(request);
        String ipSource = IpUtil.getIpSource(ipAddr);
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        // 封装权限集合
        return UserInfoDTO.builder()
                .id(user.getId())
                .loginType(user.getLoginType())
                .userInfoId(userInfo.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(userInfo.getEmail())
                .roleList(roleList)
                .nickname(userInfo.getNickname())
                .avatar(userInfo.getAvatar())
                .intro(userInfo.getIntro())
                .webSite(userInfo.getWebSite())
                .articleLikeSet(articleLikeSet)
                .commentLikeSet(commentLikeSet)
                .ipAddr(ipAddr)
                .ipSource(ipSource)
                .browser(userAgent.getBrowser().getName())
                .os(userAgent.getOperatingSystem().getName())
                .lastLoginTime(new Date())
                .build();
    }

}
