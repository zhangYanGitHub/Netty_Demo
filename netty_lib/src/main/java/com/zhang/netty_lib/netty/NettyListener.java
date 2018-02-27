package com.zhang.netty_lib.netty;


/**
 * Created by 张俨 on 2017/10/9.
 */

public interface NettyListener {

    byte STATUS_CONNECT_SUCCESS = 1;

    byte STATUS_CONNECT_CLOSED = 2;

    byte STATUS_CONNECT_ERROR = 0;


    /**
     * 对消息的处理
     */
    void onMessageResponse(String messageHolder);

    /**
     * 当服务状态发生变化时触发
     */
    void onServiceStatusConnectChanged(int statusCode);
}
