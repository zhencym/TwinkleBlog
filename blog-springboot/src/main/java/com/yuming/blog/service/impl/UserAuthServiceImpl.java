package com.yuming.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.dao.RoleDao;
import com.yuming.blog.dao.UserInfoDao;
import com.yuming.blog.dao.UserRoleDao;
import com.yuming.blog.dto.EmailDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.UserBackDTO;
import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.entity.UserInfo;
import com.yuming.blog.entity.UserAuth;
import com.yuming.blog.dao.UserAuthDao;
import com.yuming.blog.entity.UserRole;
import com.yuming.blog.enums.LoginTypeEnum;
import com.yuming.blog.enums.RoleEnum;
import com.yuming.blog.exception.ServeException;
import com.yuming.blog.handler.KafkaProducer;
import com.yuming.blog.login.SessionManager;
import com.yuming.blog.service.UserAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.utils.IpUtil;
import com.yuming.blog.utils.UserUtil;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.PasswordVO;
import com.yuming.blog.vo.UserVO;
import com.yuming.blog.constant.RedisPrefixConst;
import javax.servlet.http.HttpSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yuming.blog.constant.RedisPrefixConst.LOGIN_EXPIRE_TIME;
import static com.yuming.blog.utils.CommonUtil.checkEmail;
import static com.yuming.blog.utils.UserUtil.convertLoginUser;


@Service

public class UserAuthServiceImpl extends ServiceImpl<UserAuthDao, UserAuth> implements UserAuthService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserAuthDao userAuthDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private KafkaProducer kafkaProducer;
    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private HttpServletRequest request;

    /**
     * 邮箱号
     */
    @Value("${spring.mail.username}")
    private String email;

    /**
     * qq appId
     */
    @Value("${qq.app-id}")
    private String QQ_APP_ID;

    /**
     * qq获取用户信息接口地址
     */
    @Value("${qq.user-info-url}")
    private String QQ_USER_INFO_URL;


    @Override
    public void sendCode(String username) {
        // 校验账号是否合法
        if (!checkEmail(username)) {
            throw new ServeException("请输入正确邮箱");
        }
        // 生成六位随机验证码发送
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        // 发送验证码
        EmailDTO emailDTO = EmailDTO.builder()
                .email(username)
                .subject("验证码")
                .content("您的验证码为 " + code.toString() + " 有效期15分钟，请不要告诉他人哦！")
                .build();
        // 发送邮件存到消息队列
        kafkaProducer.sendBlogEmail(emailDTO);
        // 将验证码存入redis，设置过期时间为15分钟
        redisTemplate.boundValueOps(RedisPrefixConst.CODE_KEY + username).set(code);
        redisTemplate.expire(RedisPrefixConst.CODE_KEY + username, RedisPrefixConst.CODE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUser(UserVO user) {
        // 校验账号是否合法
        if (checkUser(user)) {
            throw new ServeException("邮箱已被注册！");
        }
        // 新增用户信息
        UserInfo userInfo = UserInfo.builder()
                .email(user.getUsername())
                .nickname(CommonConst.DEFAULT_NICKNAME)
                .avatar(CommonConst.DEFAULT_AVATAR)
                .createTime(new Date())
                .build();
        // 这里插入时，自动获取数据库自增id
        userInfoDao.insert(userInfo);
        // 绑定用户角色
        saveUserRole(userInfo);
        // 新增用户账号
        UserAuth userAuth = UserAuth.builder()
                .userInfoId(userInfo.getId())
                .username(user.getUsername())
                 //使用security的方法加密密码
                .password(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()))
                .createTime(new Date())
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();
        userAuthDao.insert(userAuth);
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePassword(UserVO user) {
        // 校验账号是否合法
        if (!checkUser(user)) {
            throw new ServeException("邮箱尚未注册！");
        }
        // 根据用户名修改密码
        userAuthDao.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getPassword, DigestUtils.md5DigestAsHex(user.getPassword().getBytes()))
                .eq(UserAuth::getUsername, user.getUsername()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAdminPassword(PasswordVO passwordVO) {
        // 查询旧密码是否正确
        UserAuth user = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getId, UserUtil.getLoginUser(request).getId()));
        // 加密
        String secret = DigestUtils.md5DigestAsHex(passwordVO.getOldPassword().getBytes());
        // 正确则修改密码，错误则提示不正确
        if (Objects.nonNull(user) && secret.equals(user.getPassword())) {
            UserAuth userAuth = UserAuth.builder()
                    .id(UserUtil.getLoginUser(request).getId())
                    .password(secret)
                    .build();
            userAuthDao.updateById(userAuth);
        } else {
            throw new ServeException("旧密码不正确");
        }
    }

    @Override
    public PageDTO<UserBackDTO> listUserBackDTO(ConditionVO condition) {
        // 转换页码
        condition.setCurrent((condition.getCurrent() - 1) * condition.getSize());
        // 获取后台用户数量
        Integer count = userAuthDao.countUser(condition);
        if (count == 0) {
            return new PageDTO<>();
        }
        // 获取后台用户列表
        List<UserBackDTO> userBackDTOList = userAuthDao.listUsers(condition);
        return new PageDTO<>(userBackDTOList, count);
    }




    /**
     * 更新用户信息
     */
    @Async
    public void updateUserInfo() {
        UserInfoDTO userInfoDTO = (UserInfoDTO)request.getSession().getAttribute("UserInfoDTO");
        if (userInfoDTO != null) {
            UserAuth userAuth = UserAuth.builder()
                .id(userInfoDTO.getId())
                .ipAddr(UserUtil.getLoginUser(request).getIpAddr())
                .ipSource(UserUtil.getLoginUser(request).getIpSource())
                .lastLoginTime(UserUtil.getLoginUser(request).getLastLoginTime())
                .build();
            // 主要更新登录ip、ip源、登陆时间
            userAuthDao.updateById(userAuth);
        }
    }


    @Override
    public void Logout(String sessionID) {
        sessionManager.deleteSession(sessionID);
    }

    /**
     * 普通登录
     * @param username 账号
     * @param pwd  密码
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfoDTO Login(String username, String pwd, HttpServletRequest request) {
        // 创建登录信息
        UserInfoDTO userInfoDTO;
        // 登录校验,校验成功则返回用户信息
        UserAuth user = getUserAuth(username, pwd);
        if (Objects.nonNull(user) && Objects.nonNull(user.getUserInfoId())) {
            // 存在则返回数据库中的用户信息登录封装
            userInfoDTO = getUserInfoDTO(user);
        } else {
            // 不存在则抛出异常
            return null;
        }
        // 将登录信息放入redis管理
//        redisTemplate.boundValueOps(RedisPrefixConst.LOGIN + userInfoDTO.getId());
//        redisTemplate.expire(RedisPrefixConst.LOGIN + userInfoDTO.getId(), LOGIN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

        // 存入session
        request.getSession().setAttribute("userInfoDTO", userInfoDTO);
        sessionManager.addSession(userInfoDTO.getUserInfoId(), request.getSession());
        // 异步更新
        updateUserInfo();
        return userInfoDTO;
    }

    /**
     * qq登录
     * 登录逻辑就是qq登录携带账号id和token，检查数据库是否存在该第三方账号，存在就更新登录信息，就成功登陆。
     * 若不存在，就创建一个新的用户信息保存这个第三方账号的信息，最后返回登录信息即可。
     * @param openId      qq openId
     * @param accessToken qq token
     * @return
     */

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfoDTO qqLogin(String openId, String accessToken, HttpServletRequest request) {
        // 创建登录信息
        UserInfoDTO userInfoDTO;
        // 校验该第三方账户信息是否存在
        // 第三方账号的唯一id 就是这个系统账户的username
        UserAuth user = getUserAuth(openId, LoginTypeEnum.QQ.getType());
        if (Objects.nonNull(user) && Objects.nonNull(user.getUserInfoId())) {
            // 存在则返回数据库中的用户信息登录封装
            userInfoDTO = getUserInfoDTO(user);
        } else {
            // 不存在通过openId和accessToken获取QQ用户信息，并创建用户
            Map<String, String> formData = new HashMap<>(16);
            // 定义请求参数
            formData.put("openid", openId);
            formData.put("access_token", accessToken);
            formData.put("oauth_consumer_key", QQ_APP_ID);
            // 获取QQ返回的用户信息
            Map<String, String> userInfoMap = JSON.parseObject(restTemplate.getForObject(QQ_USER_INFO_URL, String.class, formData), Map.class);

            // 获取ip地址
            String ipAddr = IpUtil.getIpAddr(request);
            String ipSource = IpUtil.getIpSource(ipAddr);
            // 信息存入数据库，这里没有邮箱，邮箱需要自己绑定
            UserInfo userInfo = convertUserInfo(Objects.requireNonNull(userInfoMap).get("nickname"), userInfoMap.get("figureurl_qq_1"));
            userInfoDao.insert(userInfo);
            // 将用户账号存入数据库
            UserAuth userAuth = convertUserAuth(userInfo.getId(), openId, accessToken, ipAddr, ipSource, LoginTypeEnum.QQ.getType());
            userAuthDao.insert(userAuth);
            // 绑定角色
            saveUserRole(userInfo);
            // 封装登录信息
            userInfoDTO = convertLoginUser(userAuth, userInfo, Lists.newArrayList(RoleEnum.USER.getLabel()), null, null, request);
        }

        // 将登录信息存入session
        request.getSession().setAttribute("userInfoDTO", userInfoDTO);
        sessionManager.addSession(userInfoDTO.getUserInfoId(), request.getSession());

        return userInfoDTO;
    }



    /**
     * 绑定用户角色
     *
     * @param userInfo 用户信息
     */
    private void saveUserRole(UserInfo userInfo) {
        UserRole userRole = UserRole.builder()
                .userId(userInfo.getId())
                .roleId(RoleEnum.USER.getRoleId())
                .build();
        userRoleDao.insert(userRole);
    }



    /**
     * 封装用户信息
     * 便于插入数据库UserInfo表
     *
     * @param nickname 昵称
     * @param avatar   头像
     * @return 用户信息
     */
    private UserInfo convertUserInfo(String nickname, String avatar) {
        return UserInfo.builder()
                .nickname(nickname)
                .avatar(avatar)
                .createTime(new Date())
                .build();
    }

    /**
     * 封装用户账号
     * 便于插入数据库UserAuth表
     *
     * @param userInfoId  用户信息id
     * @param uid         唯一Id标识
     * @param accessToken 登录凭证
     * @param ipAddr      ip地址
     * @param ipSource    ip来源
     * @param loginType   登录方式
     * @return 用户账号
     */
    private UserAuth convertUserAuth(Integer userInfoId, String uid, String accessToken, String ipAddr, String ipSource, Integer loginType) {
        return UserAuth.builder()
                .userInfoId(userInfoId)
                .username(uid)
                .password(accessToken)
                .loginType(loginType)
                .ipAddr(ipAddr)
                .ipSource(ipSource)
                .createTime(new Date())
                .lastLoginTime(new Date())
                .build();
    }

    /**
     * 获取本地第三方登录信息
     *
     * @param user 用户对象
     * @return 用户登录信息
     */
    private UserInfoDTO getUserInfoDTO(UserAuth user) {
        // 更新登录时间，ip
        String ipAddr = IpUtil.getIpAddr(request);
        String ipSource = IpUtil.getIpSource(ipAddr);
        userAuthDao.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getLastLoginTime, new Date())
                .set(UserAuth::getIpAddr, ipAddr)
                .set(UserAuth::getIpSource, ipSource)
                .eq(UserAuth::getId, user.getId()));
        // 查询账号对应的信息
        UserInfo userInfo = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfo>()
                .select(UserInfo::getId, UserInfo::getEmail, UserInfo::getNickname, UserInfo::getAvatar, UserInfo::getIntro, UserInfo::getWebSite, UserInfo::getIsDisable)
                .eq(UserInfo::getId, user.getUserInfoId()));
        // 查询账号点赞信息
        Set<Integer> articleLikeSet = (Set<Integer>) redisTemplate.boundHashOps("article_user_like").get(userInfo.getId().toString());
        Set<Integer> commentLikeSet = (Set<Integer>) redisTemplate.boundHashOps("comment_user_like").get(userInfo.getId().toString());
        // 查询账号角色
        List<String> roleList = roleDao.listRolesByUserInfoId(userInfo.getId());
        // 封装信息
        return convertLoginUser(user, userInfo, roleList, articleLikeSet, commentLikeSet, request);
    }


    /**
     * 检测第三方账号是否注册
     *
     * @param openId    第三方唯一id
     * @param loginType 登录方式
     * @return 用户账号信息
     */
    private UserAuth getUserAuth(String openId, Integer loginType) {
        // 查询账号信息
        return userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .select(UserAuth::getId, UserAuth::getUserInfoId, UserAuth::getLoginType)
                .eq(UserAuth::getUsername, openId)
                .eq(UserAuth::getLoginType, loginType));
    }

    /**
     * 检验账号密码是否正确
     *
     * @param username    账号
     * @param pwd       密码
     * @return 用户账号信息
     */
    private UserAuth getUserAuth(String username, String pwd) {
        UserAuth userAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
            .select(UserAuth::getId, UserAuth::getUserInfoId, UserAuth::getPassword, UserAuth::getLoginType)
            .eq(UserAuth::getUsername, username));

        // 密码正确
        if(userAuth != null && DigestUtils.md5DigestAsHex(pwd.getBytes()).equals(userAuth.getPassword())) {
            //置空再返回
            userAuth.setPassword("");
            return userAuth;
        }
        // 查询账号信息
        return null;
    }


    /**
     * 校验用户数据是否合法
     *
     * @param user 用户数据
     * @return 合法状态
     */
    private Boolean checkUser(UserVO user) {
        if (!user.getCode().equals(redisTemplate.boundValueOps(RedisPrefixConst.CODE_KEY + user.getUsername()).get())) {
            throw new ServeException("验证码错误！");
        }
        //查询用户名是否存在
        UserAuth userAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .select(UserAuth::getUsername).eq(UserAuth::getUsername, user.getUsername()));
        return Objects.nonNull(userAuth);
    }

}
