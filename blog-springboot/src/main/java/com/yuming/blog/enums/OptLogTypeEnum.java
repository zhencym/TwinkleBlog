package com.yuming.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author 86186
 * */
@Getter
@AllArgsConstructor
public enum OptLogTypeEnum {
    /**
     * 新增
     */
    ADD(1,"新增"),
    /**
     * 修改
     */
    UPDATE(2,"修改"),
    /**
     * 删除
     */
    REMOVE(3,"删除"),
    /**
     * 上传
     */
    UPLOAD(4,"上传");

    /**
     * 类型
     */
    private int type;

    /**
     * 描述
     */
    private String desc;

    public int getType() {
        return type;
    }

    public String getInfo() {
        return desc;
    }
}
