package com.yuming.blog.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 资源对象VO
 *
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceVO {
    /**
     * 资源id
     */
    private Integer id;

    /**
     * 资源名
     */
    @NotBlank(message = "资源名不能为空")
    private String resourceName;

    /**
     * 路径
     */
    private String url;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 父资源id
     */
    private Integer parentId;

    /**
     * 是否禁用
     */
    private Integer isDisable;

    /**
     * 是否匿名访问
     */
    private Integer isAnonymous;

}
