package com.cy.apkpack.websocket;

import com.cy.apkpack.Constants;
import com.cy.apkpack.bean.ResponseBaseBean;
import com.cy.apkpack.utils.LogUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/apkPack/websocket/{sid}")
@Component
public class WebSocketServer {


    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private String sid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        this.sid = sid;
        WebSocketServerManager.addWebSocketServer(this); //加入set中
        LogUtils.log("有新窗口开始监听:" + sid + ",当前在线人数为" + WebSocketServerManager.getMap_wbserver().size());
        sendMessage(new ResponseBaseBean(Constants.CODE_WEBSOCKET_OPEN,"WebSocket连接成功","WebSocket连接成功").toJson());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        WebSocketServerManager.removeWebSocketServer(this); //从set中删除
        LogUtils.log("有一连接关闭！当前在线人数为" + WebSocketServerManager.getMap_wbserver().size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        LogUtils.log("收到来自窗口" + sid + "的信息:" + message);
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LogUtils.log("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public synchronized void sendMessage(String msg) {
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return session;
    }


    public String getSid() {
        return sid;
    }


}
