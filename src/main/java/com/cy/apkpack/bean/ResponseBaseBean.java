package com.cy.apkpack.bean;

import com.alibaba.fastjson.JSON;
import com.cy.apkpack.websocket.WebSocketServerManager;

public class ResponseBaseBean<T> {
    private int code;
    private String msg;
    private T data;

    public ResponseBaseBean() {
    }

    public ResponseBaseBean(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public ResponseBaseBean setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResponseBaseBean setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public ResponseBaseBean setData(T data) {
        this.data = data;
        return this;
    }


    public ResponseBaseBean send(String sid) {
        WebSocketServerManager.sendMessage(sid, toJson());

        return this;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }
}
