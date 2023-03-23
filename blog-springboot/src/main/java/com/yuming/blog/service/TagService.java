package com.yuming.blog.service;

import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.TagDTO;
import com.yuming.blog.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.TagVO;

import java.util.List;


public interface TagService extends IService<Tag> {

    /**
     * 查询标签列表
     *
     * @return 标签列表
     */
    PageDTO<TagDTO> listTags();

    /**
     * 查询后台标签
     *
     * @param condition 条件
     * @return 标签列表
     */
    PageDTO<Tag> listTagBackDTO(ConditionVO condition);

    /**
     * 删除标签
     *
     * @param tagIdList 标签id集合
     */
    void deleteTag(List<Integer> tagIdList);

    /**
     * 保存或更新标签
     * @param tagVO 标签
     */
    void saveOrUpdateTag(TagVO tagVO);

}
