package com.yuming.blog.service;

import com.yuming.blog.dto.FriendLinkBackDTO;
import com.yuming.blog.dto.FriendLinkDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.entity.FriendLink;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.FriendLinkVO;

import java.util.List;


public interface FriendLinkService extends IService<FriendLink> {

    /**
     * 查看友链列表
     *
     * @return 友链列表
     */
    List<FriendLinkDTO> listFriendLinks();

    /**
     * 查看后台友链列表
     *
     * @param condition 条件
     * @return 友链列表
     */
    PageDTO<FriendLinkBackDTO> listFriendLinkDTO(ConditionVO condition);

    /**
     * 保存或更新友链
     * @param friendLinkVO 友链
     */
    void saveOrUpdateFriendLink(FriendLinkVO friendLinkVO);

}
