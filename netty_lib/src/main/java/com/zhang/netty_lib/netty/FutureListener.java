package com.zhang.netty_lib.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by 张俨 on 2017/10/12.
 */

public abstract class FutureListener implements ChannelFutureListener {
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            success();
        } else {
            error();
        }
    }

    public abstract void success();

    public abstract void error();
}
