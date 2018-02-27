package com.zhang.netty_lib.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

/**
 * Created by 张俨 on 2018/2/7.
 */

public abstract class NettyActivity extends AppCompatActivity {
    public final static int MSG_FROM_SERVER = 0x1;
    public final static int MSG_NET_WORK_ERROR = 0x2;
    protected String TAG;
    protected MHandler handler;

    /**
     * 暴露handler给Service
     *
     * @return
     */
    public MHandler getHandler() {
        return handler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        handler = new MHandler(this);
        TAG = this.getClass().getName();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
    }

    public static class MHandler extends Handler {
        private WeakReference<NettyActivity> activity;

        public MHandler(NettyActivity activity) {
            this.activity = new WeakReference<NettyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (activity == null || activity.get() == null) return;
            final NettyActivity nettyActivity = activity.get();
            switch (msg.what) {
                case NettyActivity.MSG_FROM_SERVER:
                    nettyActivity.notifyData((String) msg.obj);
                    break;
            }
        }
    }

    /**
     * 在主线程通知子类 刷新UI
     *
     * @param message
     */
    protected abstract void notifyData(String message);
}
