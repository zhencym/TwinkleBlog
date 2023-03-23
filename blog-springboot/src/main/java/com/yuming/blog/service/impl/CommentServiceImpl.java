package com.yuming.blog.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yuming.blog.constant.CommonConst;
import com.yuming.blog.dao.UserInfoDao;
import com.yuming.blog.dto.*;
import com.yuming.blog.dto.EmailDTO;
import com.yuming.blog.dto.ReplyCountDTO;
import com.yuming.blog.entity.Comment;
import com.yuming.blog.dao.CommentDao;
import com.yuming.blog.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuming.blog.utils.HTMLUtil;
import com.yuming.blog.utils.RedisLockUtils;
import com.yuming.blog.utils.UserUtil;
import com.yuming.blog.vo.CommentVO;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.DeleteVO;
import com.yuming.blog.constant.MQPrefixConst;
import com.yuming.blog.constant.RedisPrefixConst;
import com.yuming.blog.dto.CommentBackDTO;
import com.yuming.blog.dto.CommentDTO;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.ReplyDTO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService {
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisLockUtils redisLockUtils;

    @Override
    public PageDTO<CommentDTO> listComments(Integer articleId, Long current) {
        // 查询文章评论量
        // 这是一级评论的回复量。子评论的回复量查询在下面
        Integer commentCount = commentDao.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Objects.nonNull(articleId), Comment::getArticleId, articleId)
                .isNull(Objects.isNull(articleId), Comment::getArticleId) //articleId为null时，评论对应的ArticleId也为null
                .isNull(Comment::getParentId) //没有父标签
                .eq(Comment::getIsDelete, CommonConst.FALSE));
        if (commentCount == 0) {
            return new PageDTO<>();
        }
        // 分页查询评论集合
        // 这里首先查询的是一级评论，没有父评论
        List<CommentDTO> commentDTOList = commentDao.listComments(articleId, (current - 1) * 10);
        // 查询redis的评论点赞数据
        // 其实点赞数据可以放在数据库每条点赞记录里
        Map<String, Integer> likeCountMap = (Map<String, Integer>) redisTemplate.boundHashOps(
            RedisPrefixConst.COMMENT_LIKE_COUNT).entries();
        // 提取评论id集合，用于子评论查询
        List<Integer> commentIdList = new ArrayList<>();
        // 封装评论点赞量
        commentDTOList.forEach(item -> {
            commentIdList.add(item.getId()); //提取id
            item.setLikeCount(Objects.requireNonNull(likeCountMap).get(item.getId().toString()));//根据id设置点赞量
        });


        // 根据评论id集合查询回复数据
        // 这里查的是子评论，即回复评论的评论
        List<ReplyDTO> replyDTOList = commentDao.listReplies(commentIdList);
        // 封装回复点赞量
        replyDTOList.forEach(item -> item.setLikeCount(Objects.requireNonNull(likeCountMap).get(item.getId().toString())));

        // 根据评论id分组回复数据
        Map<Integer, List<ReplyDTO>> replyMap = replyDTOList.stream().collect(Collectors.groupingBy(ReplyDTO::getParentId));
        // 根据评论id查询回复量
        Map<Integer, Integer> replyCountMap = commentDao.listReplyCountByCommentId(commentIdList) //返回的list映射为map
                .stream().collect(Collectors.toMap(ReplyCountDTO::getCommentId, ReplyCountDTO::getReplyCount));

        // 将分页回复数据和回复量封装进对应的评论
        // 这就是map的好处，根据父id找到子评论数和子评论列表
        commentDTOList.forEach(item -> {
            item.setReplyDTOList(replyMap.get(item.getId()));
            item.setReplyCount(replyCountMap.get(item.getId()));
        });
        return new PageDTO<>(commentDTOList, commentCount);
    }

    @Override
    public List<ReplyDTO> listRepliesByCommentId(Integer commentId, Long current) {
        // 转换页码查询评论下的回复
        List<ReplyDTO> replyDTOList = commentDao.listRepliesByCommentId(commentId, (current - 1) * 5);
        // 查询redis的评论点赞数据
        Map<String, Integer> likeCountMap = (Map<String, Integer>) redisTemplate.boundHashOps(
            RedisPrefixConst.COMMENT_LIKE_COUNT).entries();
        // 封装点赞数据
        replyDTOList.forEach(item -> item.setLikeCount(Objects.requireNonNull(likeCountMap).get(item.getId().toString())));
        return replyDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveComment(CommentVO commentVO) {
        // 过滤html标签
        commentVO.setCommentContent(HTMLUtil.deleteCommentTag(commentVO.getCommentContent()));
        Comment comment = Comment.builder()
                .userId(UserUtil.getLoginUser().getUserInfoId())
                .replyId(commentVO.getReplyId())
                .articleId(commentVO.getArticleId())
                .commentContent(commentVO.getCommentContent())
                .parentId(commentVO.getParentId())
                .createTime(new Date())
                .build();
        commentDao.insert(comment);
        // 通知用户
        notice(commentVO);
    }

    /**
     * 利用消息队列发送邮件通知评论用户
     *
     * @param commentVO 评论信息
     */
    @Async //异步方法
    public void notice(CommentVO commentVO) {
        // 判断是回复用户还是评论作者
        Integer userId = Objects.nonNull(commentVO.getReplyId()) ? commentVO.getReplyId() : CommonConst.BLOGGER_ID;
        // 查询邮箱号
        String email = userInfoDao.selectById(userId).getEmail();
        if (StringUtils.isNotBlank(email)) {  //有邮箱才发邮件通知
            // 判断页面路径
            String url = Objects.nonNull(commentVO.getArticleId()) ? CommonConst.URL + CommonConst.ARTICLE_PATH + commentVO.getArticleId() : CommonConst.URL + CommonConst.LINK_PATH;
            // 发送消息
            EmailDTO emailDTO = EmailDTO.builder()
                    .email(email)
                    .subject("评论提醒")
                    .content("您收到了一条新的回复，请前往" + url + "\n页面查看")
                    .build();
            // 利用消息队列发送邮件
            // 发送消息指定一个交换机，交换机能把消息转存到对应的队列。扇形交换机无所谓routingKey，最后写上要发送的消息
            rabbitTemplate.convertAndSend(
                MQPrefixConst.EMAIL_EXCHANGE,  "*",new Message(JSON.toJSONBytes(emailDTO), new MessageProperties()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveCommentLike(Integer commentId) throws InterruptedException {
        // 分布式锁
        String value = IdUtil.randomUUID();
        // 因为COMMENT_USER_LIKE、COMMENT_LIKE_COUNT两个数据是同时使用的，不存在单独使用的情况，可以当成一个共享资源来看待。所以可以用一个锁即可
        Boolean getLock = redisLockUtils.getLock(
            RedisPrefixConst.COMMENT_USER_LIKE+ RedisPrefixConst.COMMENT_LIKE_COUNT+ RedisPrefixConst.LOCK, value);
        if (getLock) {
            // 查询当前用户点赞过的评论id集合
            HashSet<Integer> commentLikeSet = (HashSet<Integer>) redisTemplate.boundHashOps(
                RedisPrefixConst.COMMENT_USER_LIKE).get(UserUtil.getLoginUser().getUserInfoId().toString());
            // 第一次点赞则创建
            if (CollectionUtils.isEmpty(commentLikeSet)) {
                commentLikeSet = new HashSet<>();
            }
            // 判断是否点赞
            if (commentLikeSet.contains(commentId)) {
                // 点过赞则删除评论id
                commentLikeSet.remove(commentId);
                // 评论点赞量-1
                redisTemplate.boundHashOps(RedisPrefixConst.COMMENT_LIKE_COUNT).increment(commentId.toString(), -1);
            } else {
                // 未点赞则增加评论id
                commentLikeSet.add(commentId);
                // 评论点赞量+1
                redisTemplate.boundHashOps(RedisPrefixConst.COMMENT_LIKE_COUNT).increment(commentId.toString(), 1);
            }
            // 保存点赞记录
            redisTemplate.boundHashOps(RedisPrefixConst.COMMENT_USER_LIKE).put(UserUtil.getLoginUser().getUserInfoId().toString(), commentLikeSet);
            // 释放锁
            redisLockUtils.releaseLock(
                RedisPrefixConst.COMMENT_USER_LIKE+ RedisPrefixConst.COMMENT_LIKE_COUNT+ RedisPrefixConst.LOCK, value);
        } else {
            // 休眠重试获取锁
            Thread.sleep(RedisLockUtils.LOCK_REDIS_WAIT);
            this.saveCommentLike(commentId);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCommentDelete(DeleteVO deleteVO) {
        // 修改评论逻辑删除状态
        // 遍历list，每个评论id都new一个Comment，存到新List，最后更新
        List<Comment> commentList = deleteVO.getIdList().stream()
                .map(id -> Comment.builder().id(id).isDelete(deleteVO.getIsDelete()).build())
                .collect(Collectors.toList());
        this.updateBatchById(commentList);
    }

    @Override
    public PageDTO<CommentBackDTO> listCommentBackDTO(ConditionVO condition) {
        // 转换页码
        condition.setCurrent((condition.getCurrent() - 1) * condition.getSize());
        // 统计后台评论量
        Integer count = commentDao.countCommentDTO(condition);
        if (count == 0) {
            return new PageDTO<>();
        }
        // 查询后台评论集合
        List<CommentBackDTO> commentBackDTOList = commentDao.listCommentBackDTO(condition);
        // 获取评论点赞量
        Map<String, Integer> likeCountMap = redisTemplate.boundHashOps(
            RedisPrefixConst.COMMENT_LIKE_COUNT).entries();
        //封装点赞量
        commentBackDTOList.forEach(item -> item.setLikeCount(Objects.requireNonNull(likeCountMap).get(item.getId().toString())));
        return new PageDTO<>(commentBackDTOList, count);
    }

}
