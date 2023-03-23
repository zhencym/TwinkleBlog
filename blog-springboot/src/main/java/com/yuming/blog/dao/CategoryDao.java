package com.yuming.blog.dao;

import com.yuming.blog.dto.CategoryDTO;
import com.yuming.blog.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryDao extends BaseMapper<Category> {

    /**
     * 查询分类和对应文章数量
     * 分组统计分类对应的文章数量
     * @return 分类集合
     */
    List<CategoryDTO> listCategoryDTO();

}
