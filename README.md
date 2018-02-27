#Netty_Android
#### 采用netty-4.1.16.Final 于服务器保持长连接进行通讯

##### 主要思路为： 开启一个service 初始化Netty连接 
* service 类

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
                //初始化Netty
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
             * 连接初始化 认证信息
             * 发送认证信息 这个可以根据项目的实际需要数据类型 进行修改
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

* NettyClient类
        
        public class NettyClient {
        
            private static NettyClient nettyClient = new NettyClient();
        
            private EventLoopGroup group;
        
            private NettyListener listener;
        
            private Channel channel;
        
            private boolean isConnect = false;
        
            private int reconnectNum = Integer.MAX_VALUE;
        
            private long reconnectIntervalTime = 5000;
            public final static String TAG = NettyClient.class.getName();
            private final Gson gson;
            private Bootstrap bootstrap;
        
            public NettyClient() {
                gson = new Gson();
            }
        
            public static NettyClient getInstance() {
                return nettyClient;
            }
        
            public synchronized NettyClient connect() {
                if (!isConnect) {
                    group = new NioEventLoopGroup();
                    bootstrap = new Bootstrap().group(group)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .channel(NioSocketChannel.class)
                            .handler(new NettyClientInitializer(listener));
                    try {
                        ChannelFuture future = bootstrap.connect(UrlConstant.SOCKET_HOST, UrlConstant.SOCKET_PORT).sync();
                        if (future != null && future.isSuccess()) {
                            channel = future.channel();
                            isConnect = true;
                        } else {
                            isConnect = false;
                        }
        
        
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
                        reconnect();
                    }
                }
                return this;
            }
        
            public void disconnect() {
                group.shutdownGracefully();
            }
        
            public void reconnect() {
                if (reconnectNum > 0 && !isConnect) {
                    reconnectNum--;
                    try {
                        Thread.sleep(reconnectIntervalTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    disconnect();
                    connect();
                } else {
                    disconnect();
                }
            }
        
            public Channel getChannel() {
                return channel;
            }
        
            /**
             * 发送消息
             *
             * @param vo  发送消息的Json对象
             * @param futureListener 发送成功与否的监听
             */
            public void sendMessage(NettyBaseFeed vo, FutureListener futureListener) {
                boolean flag = channel != null && isConnect;
                if (!flag) {
                    Log.e(TAG, "------尚未连接");
                    return;
                }
                final String s = gson.toJson(vo);
                if (futureListener == null) {
                    channel.writeAndFlush(s).addListener(new FutureListener() {
        
                        @Override
                        public void success() {
                            Log.e(TAG, "发送成功--->" + s);
                        }
        
                        @Override
                        public void error() {
                            Log.e(TAG, "发送失败--->" + s);
                        }
                    });
                } else {
                    channel.writeAndFlush(s).addListener(futureListener);
                }
            }
        
            /**
             * 设置重连次数
             *
             * @param reconnectNum 重连次数
             */
            public void setReconnectNum(int reconnectNum) {
                this.reconnectNum = reconnectNum;
            }
        
            /**
             * 设置重连时间间隔
             *
             * @param reconnectIntervalTime 时间间隔
             */
            public void setReconnectIntervalTime(long reconnectIntervalTime) {
                this.reconnectIntervalTime = reconnectIntervalTime;
            }
        
            public boolean getConnectStatus() {
                return isConnect;
            }
        
            /**
             * 设置连接状态
             *
             * @param status
             */
            public void setConnectStatus(boolean status) {
                this.isConnect = status;
            }
        
            public void setListener(NettyListener listener) {
                if (listener == null) {
                    throw new IllegalArgumentException("listener == null ");
                }
                this.listener = listener;
            }
        
        
        }

##### 然后发送认证数据至服务器
      
        
   
* 接收服务器的消息  从NettyClientHandler的回调方法channelRead0 这里开始 调用至service之后再遍历NettyActivity的子类 通过主线程的Handler 分发至主线程

        public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
            private static final String TAG = NettyClientHandler.class.getName();
            private NettyListener listener;
        
            public NettyClientHandler(NettyListener listener) {
                this.listener = listener;
            }
        
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                NettyClient.getInstance().setConnectStatus(true);
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
            }
        
            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                NettyClient.getInstance().setConnectStatus(false);
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
                NettyClient.getInstance().reconnect();
            }
        
            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String byteBuf) throws Exception {
                Log.e(TAG, "thread == " + Thread.currentThread().getName());
                Log.e(TAG, "来自服务器的消息 ====》" + byteBuf);
                listener.onMessageResponse(byteBuf);
               
            }
* 主线程发送消息

         NettyClient.getInstance().sendMessage(baseFeed, null);
* 注意 解码器和编码器 要与服务器保持一致