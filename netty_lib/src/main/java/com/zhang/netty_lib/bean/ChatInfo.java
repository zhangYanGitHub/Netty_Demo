package com.zhang.netty_lib.bean;

/**
 * 聊天模块实体类 请求体 1-2
 */
public class ChatInfo {

    private int from;//发送人
    private int to;//接收人
    private String message;//消息内容
    private int chatType;//1.用户找客服 2.客服找用户
    private int msgType;//1.文本 //2.文件

    public ChatInfo() {
    }

    public ChatInfo(int from, int to, String message, int chatType, int msgType) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.chatType = chatType;
        this.msgType = msgType;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
