package com.yuming.blog.constant;

import lombok.Data;
import lombok.Getter;

/**
 * @Author: zhencym
 * @DATE: 2023/5/6
 */
public class Constants {

  @Getter
  public static final class OptLogType {
    /**
     * 新增
     */
    public static final String ADD = "新增";
    /**
     * 修改
     */
    public static final String UPDATE = "修改";
    /**
     * 删除
     */
    public static final String REMOVE = "删除";
    /**
     * 上传
     */
    public static final String UPLOAD = "上传";

  }
}