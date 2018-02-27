package com.zhang.netty_lib;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.zhang.netty_lib.activity.ActivityManager;
import com.zhang.netty_lib.activity.NettyActivity;
import com.zhang.netty_lib.bean.Netty_RegisterInfo;
import com.zhang.netty_lib.bean.NettyBaseFeed;
import com.zhang.netty_lib.netty.NettyClient;
import com.zhang.netty_lib.netty.NettyListener;

import java.lang.ref.WeakReference;
import java.util.Stack;

/**
 *
 */
public class NettyService extends Service implements NettyListener {

    private NetworkReceiver receiver;
    public static final String TAG = NettyService.class.getName();

    public NettyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NettyClient.getInstance().setListener(this);
        connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();
    }

    private void connect() {
        if (!NettyClient.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            }).start();
        }
    }

    @Override
    public void onMessageResponse(String messageHolder) {
        notifyData(NettyActivity.MSG_FROM_SERVER, messageHolder);

    }

    private void notifyData(int type, String messageHolder) {
        final Stack<NettyActivity> activities = ActivityManager.getInstance().getActivities();
        for (NettyActivity activity : activities) {
            if (activity == null || activity.isFinishing()) {
                continue;
            }
            Message message = Message.obtain();
            message.what = type;
            message.obj = messageHolder;
            activity.getHandler().sendMessage(message);
        }
    }

    @Override
    public void onServiceStatusConnectChanged(int statusCode) {
        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
            Log.e(TAG, "connect sucessful");
            sendAuthor();
        } else {
            Log.e(TAG, "connect fail statusCode = " + statusCode);
            notifyData(NettyActivity.MSG_NET_WORK_ERROR, String.valueOf("服务器连接失败"));
        }

    }

    /**
     * 发送认证信息
     */
    private void sendAuthor() {
        final Netty_RegisterInfo nettyRegisterInfo = new Netty_RegisterInfo();
        nettyRegisterInfo.setUserId(1);
        nettyRegisterInfo.setUserType(2);
        final NettyBaseFeed<Netty_RegisterInfo> reqRegisterVONettyBaseFeed = new NettyBaseFeed<>();
        reqRegisterVONettyBaseFeed.setCmd(1);
        reqRegisterVONettyBaseFeed.setModule(1);
        reqRegisterVONettyBaseFeed.setData(nettyRegisterInfo);
        NettyClient.getInstance().sendMessage(reqRegisterVONettyBaseFeed, null);
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    connect();
                    Log.e(TAG, "connecting ...");
                }
            }
        }
    }


}
