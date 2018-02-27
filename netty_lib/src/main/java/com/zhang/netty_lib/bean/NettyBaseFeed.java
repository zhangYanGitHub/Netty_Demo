package com.zhang.netty_lib.bean;

public class NettyBaseFeed<T> {

    /**
     * 模块号
     * 1.聊天模块
     */
    private int module;

    /**
     * 命令号
     * 1-1.注册/断线重连
     * 1-2.聊天
     */
    private int cmd;

    /**
     * 命令对象
     */
    private T data;

    public NettyBaseFeed(int module, int cmd, T data) {
        this.module = module;
        this.cmd = cmd;
        this.data = data;
    }

    public NettyBaseFeed() {
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
