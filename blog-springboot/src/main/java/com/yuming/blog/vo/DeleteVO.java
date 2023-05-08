package com.yuming.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteVO {

    /**
     * id列表
     */
    private List<Integer> idList;

    /**
     * 状态值
     */
    private Integer isDelete;

}
