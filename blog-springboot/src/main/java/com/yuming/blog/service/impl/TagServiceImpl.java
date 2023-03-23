package com.yuming.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuming.blog.dao.ArticleTagDao;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.TagDTO;
import com.yuming.blog.entity.ArticleTag;
import com.yuming.blog.entity.Tag;
import com.yuming.blog.dao.TagDao;
import com.yuming.blog.exception.ServeException;
import com.yuming.blog.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
public class TagServiceImpl extends ServiceImpl<TagDao, Tag> implements TagService {
    @Autowired
    private TagDao tagDao;
    @Autowired
    private ArticleTagDao articleTagDao;

    @Override
    public PageDTO<TagDTO> listTags() {
        // 查询标签列表
        List<Tag> tagList = tagDao.selectList(new LambdaQueryWrapper<Tag>().select(Tag::getId, Tag::getTagName));
        // 转换DTO
        List<TagDTO> tagDTOList = BeanCopyUtil.copyList(tagList, TagDTO.class);
        // 查询标签数量
        Integer count = tagDao.selectCount(null);
        return new PageDTO<>(tagDTOList, count);
    }

    @Override
    public PageDTO<Tag> listTagBackDTO(ConditionVO condition) {
        // 分页查询标签列表
        Page<Tag> page = new Page<>(condition.getCurrent(), condition.getSize());
        Page<Tag> tagPage = tagDao.selectPage(page, new LambdaQueryWrapper<Tag>()
                .select(Tag::getId, Tag::getTagName, Tag::getCreateTime)
                .like(StringUtils.isNotBlank(condition.getKeywords()), Tag::getTagName, condition.getKeywords())
                .orderByDesc(Tag::getCreateTime));
        return new PageDTO<>(tagPage.getRecords(), (int) tagPage.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTag(List<Integer> tagIdList) {
        //查询标签下是否有文章
        Integer count = articleTagDao.selectCount(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getTagId, tagIdList));
        if (count > 0) {
            throw new ServeException("删除失败，该标签下存在文章");
        }
        tagDao.deleteBatchIds(tagIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateTag(TagVO tagVO) {
        Integer count = tagDao.selectCount(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getTagName, tagVO.getTagName()));
        if (count > 0) {
            throw new ServeException("标签名已存在");
        }
        Tag tag = Tag.builder()
                .id(tagVO.getId())
                .tagName(tagVO.getTagName())
                .createTime(Objects.isNull(tagVO.getId()) ? new Date() : null)
                .build();
        this.saveOrUpdate(tag);
    }

}
