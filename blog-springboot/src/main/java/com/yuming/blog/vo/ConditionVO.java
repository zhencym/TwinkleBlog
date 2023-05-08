package com.yuming.blog.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 查询条件
 * 这里是把众多的查询条件分装成一个类，用来接受前端传递的参数。如前端传进来了，就有值，没传进来就为null。
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConditionVO {

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 标签id
     */
    private Integer tagId;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 显示数量
     */
    private Integer size;

    /**
     * 搜索内容
     */
    private String keywords;

    /**
     * 状态值
     */
    private Integer isDelete;

    /**
     * 是否为草稿
     */
    private Integer isDraft;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

}
