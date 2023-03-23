package com.yuming.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuming.blog.dao.ChatRecordDao;
import com.yuming.blog.dto.ChatRecordDTO;
import com.yuming.blog.dto.RecallMessageDTO;
import com.yuming.blog.dto.WebsocketMessageDTO;
import com.yuming.blog.entity.ChatRecord;
import com.yuming.blog.enums.FilePathEnum;
import com.yuming.blog.utils.*;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.utils.DateUtil;
import com.yuming.blog.utils.IpUtil;
import com.yuming.blog.utils.OSSUtil;
import com.yuming.blog.vo.VoiceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.yuming.blog.enums.ChatTypeEnum.*;



@ServerEndpoint(value = "/websocket",configurator = WebSocketServiceImpl.ChatConfigurator.class)
@Service
@Scope("prototype") //变成多例的bean
public class WebSocketServiceImpl {

    /**
     * 用户session
     */
    private Session session;


    /**
     * 用户session集合
     * 静态容器，记录每个客户端连接时，WebSocketServiceImpl对象。
     * concurrent包的线程安全Set。
     * 写时复制set，自动去重，线程安全
     */
    private static CopyOnWriteArraySet<WebSocketServiceImpl> webSocketSet = new CopyOnWriteArraySet<>();

    private static ChatRecordDao chatRecordDao;

    @Autowired
    public void setChatRecordDao(ChatRecordDao chatRecordDao) {
        WebSocketServiceImpl.chatRecordDao = chatRecordDao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebSocketServiceImpl that = (WebSocketServiceImpl) o;
        return Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session);
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) throws IOException {
        System.out.println("欢迎新用户！");
        // 加入连接
        this.session = session;
        webSocketSet.add(this);
        // 更新在线人数
        updateOnlineCount();
        // 加载历史聊天记录
        ChatRecordDTO chatRecordDTO = listChartRecords(endpointConfig);
        // 发送消息
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(HISTORY_RECORD.getType())
                .data(chatRecordDTO)
                .build();
        synchronized (session) {
            session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
        }
    }

    /**
     * 获取客户端真实ip
     */
    public static class ChatConfigurator extends ServerEndpointConfig.Configurator {

        public static String HEADER_NAME = "X-Real-IP";

        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            try {
                String firstFoundHeader = request.getHeaders().get(HEADER_NAME.toLowerCase()).get(0);
                sec.getUserProperties().put(HEADER_NAME, firstFoundHeader);
            } catch (Exception e) {
                sec.getUserProperties().put(HEADER_NAME, "未知ip");
            }
        }

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        // 更新在线人数
        webSocketSet.remove(this);
        updateOnlineCount();
    }

    /**
     * 加载历史聊天记录
     *
     * @param endpointConfig 配置
     * @return 加载历史聊天记录
     */
    private ChatRecordDTO listChartRecords(EndpointConfig endpointConfig) {
        List<ChatRecord> chatRecordList = chatRecordDao.selectList(new LambdaQueryWrapper<ChatRecord>()
                .ge(ChatRecord::getCreateTime, DateUtil.getBeforeHourTime(24))); //加载24小时前的聊天记录
        String ipAddr = endpointConfig.getUserProperties().get(ChatConfigurator.HEADER_NAME).toString();
        return ChatRecordDTO.builder()
                .chatRecordList(chatRecordList)
                .ipAddr(ipAddr)
                .ipSource(IpUtil.getIpSource(ipAddr))
                .build();
    }

    /**
     * 更新在线人数
     *
     * @throws IOException io异常
     */
    private void updateOnlineCount() throws IOException {
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(ONLINE_COUNT.getType())
                .data(webSocketSet.size())
                .build();
        for (WebSocketServiceImpl webSocketService : webSocketSet) {
            synchronized (webSocketService.session) { //给每个客户端发送在线人数消息
                webSocketService.session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
            }
        }
    }

    /**
     * 收到客户端消息后调用的方法
     * message是json格式的数据类型，可转化为一个WebsocketMessageDTO对象
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        WebsocketMessageDTO messageDTO = JSON.parseObject(message, WebsocketMessageDTO.class);
        switch (Objects.requireNonNull(getChatType(messageDTO.getType()))) { //Type这个数据哪来的
            case SEND_MESSAGE:
                // 发送消息
                ChatRecord chatRecord = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), ChatRecord.class);
                chatRecordDao.insert(chatRecord);
                messageDTO.setData(chatRecord);
                // 群发
                for (WebSocketServiceImpl webSocketService : webSocketSet) {
                    synchronized (webSocketService.session) {
                        webSocketService.session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
                    }
                }
                break;
            case RECALL_MESSAGE:
                // 撤回消息
                RecallMessageDTO recallMessage = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), RecallMessageDTO.class);
                // 删除记录
                deleteRecord(recallMessage.getId());
                for (WebSocketServiceImpl webSocketService : webSocketSet) {
                    synchronized (webSocketService.session) {
                        webSocketService.session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
                    }
                }
                break;
            case HEART_BEAT:
                // 心跳消息
                messageDTO.setData("pong");
                session.getBasicRemote().sendText(JSON.toJSONString(JSON.toJSONString(messageDTO)));
            default:
                break;
        }

    }

    /**
     * 删除记录
     *
     * @param id ID
     */
    @Async
    public void deleteRecord(Integer id) {
        chatRecordDao.deleteById(id);
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 发送语音
     *
     * @param voiceVO 语音路径
     */
    public void sendVoice(VoiceVO voiceVO) throws IOException {
        // 上传语音文件
        String content = OSSUtil.upload(voiceVO.getFile(), FilePathEnum.VOICE.getPath());
        voiceVO.setContent(content);
        // 保存记录
        ChatRecord chatRecord = BeanCopyUtil.copyObject(voiceVO, ChatRecord.class);
        chatRecordDao.insert(chatRecord);
        // 发送消息
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(VOICE_MESSAGE.getType())
                .data(chatRecord)
                .build();
        for (WebSocketServiceImpl webSocketService : webSocketSet) {
            synchronized (webSocketService.session) {
                webSocketService.session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
            }
        }
    }

}
