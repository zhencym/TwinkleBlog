package com.yuming.blog.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 音频VO
 *
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoiceVO {

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 文件
     * 文件类型不能序列化，否则报错
     */
    @JSONField(serialize = false)
    private MultipartFile file;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 聊天内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户登录ip
     */
    private String ipAddr;

    /**
     * ip来源
     */
    private String ipSource;

}
