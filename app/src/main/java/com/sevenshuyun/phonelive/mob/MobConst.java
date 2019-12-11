package com.sevenshuyun.phonelive.mob;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.twitter.Twitter;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;


public class MobConst {

    public static final Map<String, String> MAP;

    static {
        MAP = new HashMap<>();
        MAP.put(Type.QQ, QQ.NAME);
        MAP.put(Type.QZONE, QZone.NAME);
        MAP.put(Type.WX, Wechat.NAME);
        MAP.put(Type.WX_PYQ, WechatMoments.NAME);
        MAP.put(Type.FACEBOOK, Facebook.NAME);
        MAP.put(Type.TWITTER, Twitter.NAME);
    }

    public static class Type {
        public static final String QQ = "qq";
        public static final String QZONE = "qzone";
        public static final String WX = "wx";
        public static final String WX_PYQ = "wchat";
        public static final String FACEBOOK = "facebook";
        public static final String TWITTER = "twitter";
    }

}
