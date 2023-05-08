package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.FriendLinkBackDTO;
import com.yuming.blog.dto.FriendLinkDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.service.FriendLinkService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.FriendLinkVO;
import com.yuming.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 友链
 */
@RestController
public class FriendLinkController {
    @Autowired
    private FriendLinkService friendLinkService;

    /**
     * 查看友链列表
     * @return
     */
    @GetMapping("/links")
    public Result<List<FriendLinkDTO>> listFriendLinks() {
        return new Result<>(true, StatusConst.OK, "查询成功", friendLinkService.listFriendLinks());
    }

    /**
     * 查看后台友链列表
     * @param condition
     * @return
     */
    @GetMapping("/admin/links")
    public Result<PageDTO<FriendLinkBackDTO>> listFriendLinkDTO(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "查询成功", friendLinkService.listFriendLinkDTO(condition));
    }

    /**
     * 保存或修改友链
     * @param friendLinkVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.FRIEND_LINK, optDesc = "保存或修改友链")
    @PostMapping("/admin/links")
    public Result saveOrUpdateFriendLink(@Valid @RequestBody FriendLinkVO friendLinkVO) {
        friendLinkService.saveOrUpdateFriendLink(friendLinkVO);
        return new Result<>(true, StatusConst.OK, "操作成功");
    }

    /**
     * 删除友链
     * @param linkIdList
     * @return
     */
    @OptLog(optType = OptLogType.REMOVE, optModule = OptLogModule.FRIEND_LINK, optDesc = "删除友链")
    @DeleteMapping("/admin/links")
    public Result deleteFriendLink(@RequestBody List<Integer> linkIdList) {
        friendLinkService.removeByIds(linkIdList);
        return new Result<>(true, StatusConst.OK, "删除成功");
    }

}

