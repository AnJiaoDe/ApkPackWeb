package com.cy.apkpack.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class WebSocketServerManager {
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentMap<String, WebSocketServer> map_wbserver = new ConcurrentHashMap();

    private  WebSocketServerManager(){

    }
//    private static class WebSocketServerManagerFactory{
//        private static WebSocketServerManager instance=new WebSocketServerManager();
//    }
//    public static WebSocketServerManager getInstance(){
//        return WebSocketServerManagerFactory.instance;
//    }
    public static void addWebSocketServer(WebSocketServer webSocketServer){
        map_wbserver.put(webSocketServer.getSid(),webSocketServer);

    }
    public static void removeWebSocketServer(WebSocketServer webSocketServer){
        map_wbserver.remove(webSocketServer.getSid());
    }
    public static void sendMessage(String sid, String msg){

        map_wbserver.get(sid).sendMessage(msg);
    }
    public static ConcurrentMap<String, WebSocketServer> getMap_wbserver() {
        return map_wbserver;
    }


}












