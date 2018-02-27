package com.zhang.netty_lib.bean;

/**
 * 请求注册实体类 请求体1-1
 */
public class Netty_RegisterInfo {

    private int userType;//1.普通用户 2.客服
    private int userId;//用户的id,用户:memberId,客服:sellerId

    public Netty_RegisterInfo() {
    }

    public Netty_RegisterInfo(int userType, int userId) {
        this.userType = userType;
        this.userId = userId;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
