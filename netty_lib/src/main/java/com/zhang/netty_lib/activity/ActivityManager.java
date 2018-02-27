package com.zhang.netty_lib.activity;

import java.util.Stack;

/**
 * Created by 张俨 on 2018/2/7.
 */

public class ActivityManager {

    private static ActivityManager acitivityManager = new ActivityManager();
    public Stack<NettyActivity> activities = new Stack<>();

    public static ActivityManager getInstance() {
        return acitivityManager;
    }

    public Stack<NettyActivity> getActivities() {
        return activities;
    }

    public void addActivity(NettyActivity activity) {
        if (activity == null) {
            return;
        }
        activities.add(activity);
    }

    public void removeActivity(NettyActivity activity) {
        if (activity == null) {
            return;
        }
        activities.remove(activity);
    }
}
