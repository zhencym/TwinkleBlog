package com.yuming.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuming.blog.dto.FriendLinkBackDTO;
import com.yuming.blog.dto.FriendLinkDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.entity.FriendLink;
import com.yuming.blog.dao.FriendLinkDao;
import com.yuming.blog.service.FriendLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.FriendLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkDao, FriendLink> implements FriendLinkService {
    @Autowired
    private FriendLinkDao friendLinkDao;

    @Override
    public List<FriendLinkDTO> listFriendLinks() {
        // 查询友链列表
        List<FriendLink> friendLinkList = friendLinkDao.selectList(new LambdaQueryWrapper<FriendLink>()
                .select(FriendLink::getId, FriendLink::getLinkName, FriendLink::getLinkAvatar, FriendLink::getLinkAddress, FriendLink::getLinkIntro));
        return BeanCopyUtil.copyList(friendLinkList, FriendLinkDTO.class);
    }

    @Override
    public PageDTO<FriendLinkBackDTO> listFriendLinkDTO(ConditionVO condition) {
        // 分页查询友链列表（还有实现了模糊查询）
        Page<FriendLink> page = new Page<>(condition.getCurrent(), condition.getSize());
        Page<FriendLink> friendLinkPage = friendLinkDao.selectPage(page, new LambdaQueryWrapper<FriendLink>()
                .select(FriendLink::getId, FriendLink::getLinkName, FriendLink::getLinkAvatar, FriendLink::getLinkAddress, FriendLink::getLinkIntro, FriendLink::getCreateTime)
                .like(StringUtils.isNotBlank(condition.getKeywords()), FriendLink::getLinkName, condition.getKeywords()));
        // 转换DTO（两个类一模一样还要转换，太规范了！）
        List<FriendLinkBackDTO> friendLinkBackDTOList = BeanCopyUtil.copyList(friendLinkPage.getRecords(), FriendLinkBackDTO.class);
        return new PageDTO<>(friendLinkBackDTOList, (int) friendLinkPage.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateFriendLink(FriendLinkVO friendLinkVO) {
        FriendLink friendLink = FriendLink.builder()
                .id(friendLinkVO.getId())
                .linkName(friendLinkVO.getLinkName())
                .linkAvatar(friendLinkVO.getLinkAvatar())
                .linkAddress(friendLinkVO.getLinkAddress())
                .linkIntro(friendLinkVO.getLinkIntro())
                .createTime(Objects.isNull(friendLinkVO.getId()) ? new Date() : null)
                .build();
        this.saveOrUpdate(friendLink);
    }
    // TODO 实现删除

}
