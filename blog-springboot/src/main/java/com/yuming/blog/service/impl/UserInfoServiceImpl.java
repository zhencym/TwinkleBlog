package com.yuming.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.dto.UserOnlineDTO;
import com.yuming.blog.entity.UserInfo;
import com.yuming.blog.dao.UserInfoDao;
import com.yuming.blog.entity.UserRole;
import com.yuming.blog.enums.FilePathEnum;
import com.yuming.blog.exception.ServeException;
import com.yuming.blog.login.SessionManager;
import com.yuming.blog.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.service.UserRoleService;
import com.yuming.blog.utils.OSSUtil;

import com.yuming.blog.utils.UserUtil;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.EmailVO;
import com.yuming.blog.vo.UserInfoVO;
import com.yuming.blog.vo.UserRoleVO;
import com.yuming.blog.constant.RedisPrefixConst;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoDao, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private HttpServletRequest request;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserInfo(UserInfoVO userInfoVO) {
        // 封装用户信息
        UserInfo userInfo = UserInfo.builder()
                .id(UserUtil.getLoginUser(request).getUserInfoId())
                .nickname(userInfoVO.getNickname())
                .intro(userInfoVO.getIntro())
                .webSite(userInfoVO.getWebSite())
                .updateTime(new Date())
                .build();
        userInfoDao.updateById(userInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String updateUserAvatar(MultipartFile file) {
        // 头像上传oss，返回图片地址
        String avatar = OSSUtil.upload(file, FilePathEnum.AVATAR.getPath());
        // 更新用户信息
        UserInfo userInfo = UserInfo.builder()
                .id(UserUtil.getLoginUser(request).getUserInfoId())
                .avatar(avatar)
                .build();
        userInfoDao.updateById(userInfo);
        return avatar;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUserEmail(EmailVO emailVO) {
        if (!emailVO.getCode().equals(redisTemplate.boundValueOps(RedisPrefixConst.CODE_KEY + emailVO.getEmail()).get())) {
            throw new ServeException("验证码错误！");
        }
        UserInfo userInfo = UserInfo.builder()
                .id(UserUtil.getLoginUser(request).getUserInfoId())
                .email(emailVO.getEmail())
                .build();
        userInfoDao.updateById(userInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserRole(UserRoleVO userRoleVO) {
        // 更新用户角色和昵称
        UserInfo userInfo = UserInfo.builder()
                .id(userRoleVO.getUserInfoId())
                .nickname(userRoleVO.getNickname())
                .build();
        userInfoDao.updateById(userInfo);
        // 删除用户角色重新添加
        userRoleService.remove(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userRoleVO.getUserInfoId()));
        // 把角色id列表 映射成 用户id角色id列表
        List<UserRole> userRoleList = userRoleVO.getRoleIdList().stream()
                .map(roleId -> UserRole.builder()
                        .roleId(roleId)
                        .userId(userRoleVO.getUserInfoId())
                        .build())
                .collect(Collectors.toList());
        userRoleService.saveBatch(userRoleList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserDisable(Integer userInfoId, Integer isDisable) {
        // 更新用户禁用状态
        UserInfo userInfo = UserInfo.builder()
                .id(userInfoId)
                .isDisable(isDisable)
                .build();
        userInfoDao.updateById(userInfo);
    }

    @Override
    public PageDTO<UserOnlineDTO> listOnlineUsers(ConditionVO conditionVO) {
        // 获取session中所有用户信息
        List<UserInfoDTO> userInfoDTOList = sessionManager.getUserInfoDTOList();
        // dto转换
        List<UserOnlineDTO> userOnlineDTOList = userInfoDTOList.stream().map(
            item -> UserOnlineDTO.builder()
                .userInfoId(item.getUserInfoId())
                .nickname(item.getNickname())
                .avatar(item.getAvatar())
                .ipAddr(item.getIpAddr())
                .ipSource(item.getIpSource())
                .browser(item.getBrowser())
                .os(item.getOs())
                .lastLoginTime(item.getLastLoginTime())
                .build())
            // 按最后一次上登录时间逆序排序
            .sorted(Comparator.comparing(UserOnlineDTO::getLastLoginTime).reversed())
            .collect(Collectors.toList());

        // 执行分页
        int current = (conditionVO.getCurrent() - 1) * conditionVO.getSize();
        int size = userOnlineDTOList.size() > conditionVO.getSize() ? current + conditionVO.getSize() : userOnlineDTOList.size();
        List<UserOnlineDTO> userOnlineList = userOnlineDTOList.subList((conditionVO.getCurrent() - 1) * conditionVO.getSize(), size);
        return new PageDTO<>(userOnlineList, userOnlineDTOList.size());
    }

    @Override
    public void removeOnlineUser(Integer userInfoId) {
        sessionManager.expireSession(userInfoId);
    }

}
