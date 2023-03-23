package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.dao.ElasticsearchDao;
import com.yuming.blog.dto.ArticleSearchDTO;
import com.yuming.blog.entity.Article;
import com.yuming.blog.constant.MQPrefixConst;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 同步es数据
 * MQ消费者，消费EMAXWELL_QUEUE文章消息队列，更新文章内容，同步es数据
 * 数据库的文章更新了，es中的文章数据也要更新，通过MQ来异步实现
 *
 *
 */
@Component
@RabbitListener(queues = MQPrefixConst.MAXWELL_QUEUE)
public class MaxWellReceiver {
    @Autowired
    private ElasticsearchDao elasticsearchDao;

    @RabbitHandler
    public void process(byte[] data) {
        //获取更改信息
        Map<String, Object> map = JSON.parseObject(new String(data), Map.class);
        //获取文章数据
        Article article = JSONObject.toJavaObject((JSONObject) map.get("data"), Article.class);
        //判断操作类型
        String type = map.get("type").toString();
        // 数据库的文章更新了，es中的文章数据也要更新，通过MQ来异步实现
        switch (type) {
            case "insert":
            case "update":
                // 发布文章后更新es文章
                if (article.getIsDraft().equals(CommonConst.FALSE)) {
                    elasticsearchDao.save(convertArticleSearchDTO(article));
                }
                break;
            case "delete":
                // 物理删除文章
                if (article.getIsDraft().equals(CommonConst.FALSE)) {
                    elasticsearchDao.deleteById(article.getId());
                }
            default:
                break;
        }
    }


    /**
     * 转换文章搜索DTO
     *
     * @param article 文章
     * @return 文章搜索DTO
     */
    private ArticleSearchDTO convertArticleSearchDTO(Article article) {
        return ArticleSearchDTO.builder()
                .id(article.getId())
                .articleTitle(article.getArticleTitle())
                .articleContent(article.getArticleContent())
                .isDelete(article.getIsDelete())
                .build();
    }

}