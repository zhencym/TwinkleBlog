package com.yuming.blog.service.impl;

import com.yuming.blog.entity.ArticleTag;
import com.yuming.blog.dao.ArticleTagDao;
import com.yuming.blog.service.ArticleTagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagDao, ArticleTag> implements ArticleTagService {

}
