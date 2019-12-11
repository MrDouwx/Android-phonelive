package com.sevenshuyun.phonelive.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.ErrorActivity;
import com.sevenshuyun.phonelive.bean.ConfigBean;
import com.sevenshuyun.phonelive.bean.TxLocationBean;
import com.sevenshuyun.phonelive.bean.TxLocationPoiBean;
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.event.FollowEvent;
import com.sevenshuyun.phonelive.im.ImPushUtil;
import com.sevenshuyun.phonelive.interfaces.CommonCallback;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.Logger;
import com.sevenshuyun.phonelive.utils.MD5Util;
import com.sevenshuyun.phonelive.utils.SpUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

/**
 * Created by cxf on 2018/9/17.
 */

public class HttpUtil {

    private static final String SALT = "76576076c1f5f657b634e966c8836a06";
    private static final String DEVICE = "android";
    private static final String VIDEO_SALT = "#2hgfk85cm23mk58vncsark";

    /**
     * 初始化
     */
    public static void init() {
        HttpClient.getInstance().init();
    }

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }

    /**
     * 使用腾讯定位sdk获取 位置信息
     *
     * @param lng 经度
     * @param lat 纬度
     * @param poi 是否要查询POI
     */
    public static void getAddressInfoByTxLocaitonSdk(final double lng, final double lat, final int poi, int pageIndex, String tag, final CommonCallback<TxLocationBean> commonCallback) {
        OkGo.<String>get("https://apis.map.qq.com/ws/geocoder/v1/")
                .params("location", lat + "," + lng)
                .params("get_poi", poi)
                .params("poi_options", "address_format=short;radius=1000;page_size=20;page_index=" + pageIndex + ";policy=5")
                .params("key", AppConfig.getInstance().getTxLocationKey())
                .tag(tag)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSON.parseObject(response.body());
                        if (obj.getIntValue("status") == 0) {
                            JSONObject result = obj.getJSONObject("result");
                            if (result != null) {
                                TxLocationBean bean = new TxLocationBean();
                                bean.setLng(lng);
                                bean.setLat(lat);
                                bean.setAddress(result.getString("address"));
                                JSONObject addressComponent = result.getJSONObject("address_component");
                                if (addressComponent != null) {
                                    bean.setNation(addressComponent.getString("nation"));
                                    bean.setProvince(addressComponent.getString("province"));
                                    bean.setCity(addressComponent.getString("city"));
                                    bean.setDistrict(addressComponent.getString("district"));
                                    bean.setStreet(addressComponent.getString("street"));
                                }
                                if (poi == 1) {
                                    List<TxLocationPoiBean> poiList = JSON.parseArray(result.getString("pois"), TxLocationPoiBean.class);
                                    bean.setPoiList(poiList);
                                }
                                if (commonCallback != null) {
                                    commonCallback.callback(bean);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * 使用腾讯地图API进行搜索
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static void searchAddressInfoByTxLocaitonSdk(final double lng, final double lat, String keyword, int pageIndex, final CommonCallback<List<TxLocationPoiBean>> commonCallback) {
        OkGo.<String>get("https://apis.map.qq.com/ws/place/v1/search?")
                .params("keyword", keyword)
                .params("boundary", "nearby(" + lat + "," + lng + ",1000)&orderby=_distance&page_size=20&page_index=" + pageIndex)
                .params("key", AppConfig.getInstance().getTxLocationKey())
                .tag(HttpConsts.GET_MAP_SEARCH)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSON.parseObject(response.body());
                        if (obj.getIntValue("status") == 0) {
                            List<TxLocationPoiBean> poiList = JSON.parseArray(obj.getString("data"), TxLocationPoiBean.class);
                            if (commonCallback != null) {
                                commonCallback.callback(poiList);
                            }
                        }
                    }
                });
    }

    /**
     * 验证token是否过期
     */
    public static void ifToken(String uid, String token, HttpCallback callback) {
        HttpClient.getInstance().get("User.iftoken", HttpConsts.IF_TOKEN)
                .params("uid", uid)
                .params("token", token)
                .execute(callback);
    }

    /**
     * 获取config
     */
    public static void getConfig(final CommonCallback<ConfigBean> commonCallback) {
        HttpClient.getInstance().get("Home.getConfig", HttpConsts.GET_CONFIG)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            try {
                                Logger.debug(info[0]);
                                JSONObject obj = JSON.parseObject(info[0]);
                                ConfigBean bean = JSON.toJavaObject(obj, ConfigBean.class);
                                AppConfig.getInstance().setConfig(bean);
                                AppConfig.getInstance().setLevel(obj.getString("level"));
                                AppConfig.getInstance().setAnchorLevel(obj.getString("levelanchor"));
                                SpUtil.getInstance().setStringValue(SpUtil.CONFIG, info[0]);
                                if (commonCallback != null) {
                                    commonCallback.callback(bean);
                                }
                            } catch (Exception e) {
                                String error = "info[0]:" + info[0] + "\n\n\n" + "Exception:" + e.getClass() + "---message--->" + e.getMessage();
                                Logger.debug("GetConfig接口返回数据异常"+error);
                                ErrorActivity.forward("GetConfig接口返回数据异常", error);
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        Logger.debug("GetConfig接口返回数据onError");
                        if (commonCallback != null) {
                            commonCallback.callback(null);
                        }
                    }
                });
    }

    /**
     * 获取用户信息
     */
    public static void getBaseInfo(final CommonCallback<UserBean> commonCallback) {
        HttpClient.getInstance().get("User.getBaseInfo", HttpConsts.GET_BASE_INFO)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            JSONObject obj = JSON.parseObject(info[0]);
                            UserBean bean = JSON.toJavaObject(obj, UserBean.class);
                            AppConfig.getInstance().setUserBean(bean);
                            AppConfig.getInstance().setUserItemList(obj.getString("list"));
                            SpUtil.getInstance().setStringValue(SpUtil.USER_INFO, info[0]);
                            if (commonCallback != null) {
                                commonCallback.callback(bean);
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        if (commonCallback != null) {
                            commonCallback.callback(null);
                        }
                    }
                });
    }


    /**
     * 手机号 密码登录
     */
    public static void login(String phoneNum, String pwd, HttpCallback callback) {
        HttpClient.getInstance().get("Login.userLogin", HttpConsts.LOGIN)
                .params("user_login", phoneNum)
                .params("user_pass", pwd)
                .params("pushid", ImPushUtil.getInstance().getPushID())
                .execute(callback);

    }

    /**
     * 第三方登录
     */
    public static void loginByThird(String openid, String nicename, String avatar, String type, HttpCallback callback) {
        String sign = MD5Util.getMD5("openid=" + openid + "&" + SALT);
        HttpClient.getInstance().get("Login.userLoginByThird", HttpConsts.LOGIN_BY_THIRD)
                .params("openid", openid)
                .params("nicename", nicename)
                .params("avatar", avatar)
                .params("type", type)
                .params("source", DEVICE)
                .params("sign", sign)
                .params("pushid", ImPushUtil.getInstance().getPushID())
                .execute(callback);
    }

    /**
     * QQ登录的时候 获取unionID 与PC端互通的时候用
     */
    public static void getQQLoginUnionID(String accessToken, final CommonCallback<String> commonCallback) {
        OkGo.<String>get("https://graph.qq.com/oauth2.0/me?access_token=" + accessToken + "&unionid=1")
                .tag(HttpConsts.GET_QQ_LOGIN_UNION_ID)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (commonCallback != null) {
                            String data = response.body();
                            data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
                            L.e("getQQLoginUnionID------>" + data);
                            JSONObject obj = JSON.parseObject(data);
                            commonCallback.callback(obj.getString("unionid"));
                        }
                    }
                });
    }

    /**
     * 获取验证码接口 注册用
     */
    public static void getRegisterCode(String mobile, HttpCallback callback) {
        String sign = MD5Util.getMD5("mobile=" + mobile + "&" + SALT);
        HttpClient.getInstance().get("Login.getCode", HttpConsts.GET_REGISTER_CODE)
                .params("mobile", mobile)
                .params("sign", sign)
                .execute(callback);
    }

    /**
     * 手机注册接口
     */
    public static void register(String user_login, String pass, String pass2, String code, HttpCallback callback) {
        HttpClient.getInstance().get("Login.userReg", HttpConsts.REGISTER)
                .params("user_login", user_login)
                .params("user_pass", pass)
                .params("user_pass2", pass2)
                .params("code", code)
                .params("source", DEVICE)
                .execute(callback);
    }

    /**
     * 找回密码
     */
    public static void findPwd(String user_login, String pass, String pass2, String code, HttpCallback callback) {
        HttpClient.getInstance().get("Login.userFindPass", HttpConsts.FIND_PWD)
                .params("user_login", user_login)
                .params("user_pass", pass)
                .params("user_pass2", pass2)
                .params("code", code)
                .execute(callback);
    }


    /**
     * 重置密码
     */
    public static void modifyPwd(String oldpass, String pass, String pass2, HttpCallback callback) {
        HttpClient.getInstance().get("User.updatePass", HttpConsts.MODIFY_PWD)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("oldpass", oldpass)
                .params("pass", pass)
                .params("pass2", pass2)
                .execute(callback);
    }


    /**
     * 获取验证码接口 找回密码用
     */
    public static void getFindPwdCode(String mobile, HttpCallback callback) {
        String sign = MD5Util.getMD5("mobile=" + mobile + "&" + SALT);
        HttpClient.getInstance().get("Login.getForgetCode", HttpConsts.GET_FIND_PWD_CODE)
                .params("mobile", mobile)
                .params("sign", sign)
                .execute(callback);
    }


    /**
     * 首页直播
     */
    public static void getHot(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.getHot", HttpConsts.GET_HOT)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 首页 附近
     */
    public static void getNear(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.getNearby", HttpConsts.GET_NEAR)
                .params("lng", AppConfig.getInstance().getLng())
                .params("lat", AppConfig.getInstance().getLat())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 首页
     */
    public static void getFollow(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.getFollow", HttpConsts.GET_FOLLOW)
                .params("uid", AppConfig.getInstance().getUid())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 分类直播
     */
    public static void getClassLive(int classId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.getClassLive", HttpConsts.GET_CLASS_LIVE)
                .params("liveclassid", classId)
                .params("p", p)
                .execute(callback);
    }


    //排行榜  收益榜
    public static void profitList(String type, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.profitList", HttpConsts.PROFIT_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("type", type)
                .params("p", p)
                .execute(callback);
    }

    //排行榜  贡献榜
    public static void consumeList(String type, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.consumeList", HttpConsts.CONSUME_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("type", type)
                .params("p", p)
                .execute(callback);

    }

    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(int from, String touid, CommonCallback<Integer> callback) {
        setAttention(HttpConsts.SET_ATTENTION, from, touid, callback);
    }

    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(String tag, final int from, final String touid, final CommonCallback<Integer> callback) {
        if (touid.equals(AppConfig.getInstance().getUid())) {
            ToastUtil.show(WordUtil.getString(R.string.cannot_follow_self));
            return;
        }
        HttpClient.getInstance().get("User.setAttent", tag)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            int isAttention = JSON.parseObject(info[0]).getIntValue("isattent");//1是 关注  0是未关注
                            EventBus.getDefault().post(new FollowEvent(from, touid, isAttention));
                            if (callback != null) {
                                callback.callback(isAttention);
                            }
                        }
                    }
                });
    }

    /**
     * 上传头像，用post
     */
    public static void updateAvatar(File file, HttpCallback callback) {
        HttpClient.getInstance().post("User.updateAvatar", HttpConsts.UPDATE_AVATAR)
                .isMultipart(true)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("file", file)
                .execute(callback);
    }

    /**
     * 更新用户资料
     *
     * @param fields 用户资料 ,以json形式出现
     */
    public static void updateFields(String fields, HttpCallback callback) {
        HttpClient.getInstance().get("User.updateFields", HttpConsts.UPDATE_FIELDS)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("fields", fields)
                .execute(callback);
    }


    /**
     * 获取对方的关注列表
     *
     * @param touid 对方的uid
     */
    public static void getFollowList(String touid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("User.getFollowsList", HttpConsts.GET_FOLLOW_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取对方的粉丝列表
     *
     * @param touid 对方的uid
     */
    public static void getFansList(String touid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("User.getFansList", HttpConsts.GET_FANS_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("p", p)
                .execute(callback);

    }

    /**
     * 获取用户的直播记录
     *
     * @param touid 对方的uid
     */
    public static void getLiveRecord(String touid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("User.getLiverecord", HttpConsts.GET_LIVE_RECORD)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取个性设置列表
     */
    public static void getSettingList(HttpCallback callback) {
        HttpClient.getInstance().get("User.getPerSetting", HttpConsts.GET_SETTING_LIST)
                .execute(callback);
    }

    /**
     * 请求签到奖励
     */
    public static void requestBonus(HttpCallback callback) {
        HttpClient.getInstance().get("User.Bonus", HttpConsts.REQUEST_BONUS)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 获取签到奖励
     */
    public static void getBonus(HttpCallback callback) {
        HttpClient.getInstance().get("User.getBonus", HttpConsts.GET_BONUS)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 主播开播
     *
     * @param title    直播标题
     * @param type     直播类型 普通 密码 收费等
     * @param typeVal  密码 价格等
     * @param file     封面图片文件
     * @param callback
     */
    public static void createRoom(String title, int liveClassId, int type, int typeVal, File file, HttpCallback callback) {
        AppConfig appConfig = AppConfig.getInstance();
        UserBean u = appConfig.getUserBean();
        if (u == null) {
            return;
        }
        PostRequest<JsonBean> request = HttpClient.getInstance().post("Live.createRoom", HttpConsts.CREATE_ROOM)
                .params("uid", appConfig.getUid())
                .params("token", appConfig.getToken())
                .params("user_nicename", u.getUserNiceName())
                .params("avatar", u.getAvatar())
                .params("avatar_thumb", u.getAvatarThumb())
                .params("city", appConfig.getCity())
                .params("province", appConfig.getProvince())
                .params("lat", appConfig.getLat())
                .params("lng", appConfig.getLng())
                .params("title", title)
                .params("liveclassid", liveClassId)
                .params("type", type)
                .params("type_val", typeVal);
        if (file != null) {
            request.params("file", file);
        }
        request.execute(callback);
    }

    /**
     * 修改直播状态
     */
    public static void changeLive(String stream) {
        HttpClient.getInstance().get("Live.changeLive", HttpConsts.CHANGE_LIVE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("status", "1")
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        L.e("开播---changeLive---->" + info[0]);
                    }
                });
    }

    /**
     * 主播结束直播
     */
    public static void stopLive(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.stopRoom", HttpConsts.STOP_LIVE)
                .params("stream", stream)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 直播结束后，获取直播收益，观看人数，时长等信息
     */
    public static void getLiveEndInfo(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.stopInfo", HttpConsts.GET_LIVE_END_INFO)
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 检查直播间状态，是否收费 是否有密码等
     *
     * @param liveUid 主播的uid
     * @param stream  主播的stream
     */
    public static void checkLive(String liveUid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.checkLive", HttpConsts.CHECK_LIVE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 当直播间是门票收费，计时收费或切换成计时收费的时候，观众请求这个接口
     *
     * @param liveUid 主播的uid
     * @param stream  主播的stream
     */
    public static void roomCharge(String liveUid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.roomCharge", HttpConsts.ROOM_CHARGE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("liveuid", liveUid)
                .execute(callback);

    }

    /**
     * 当直播间是计时收费的时候，观众每隔一分钟请求这个接口进行扣费
     *
     * @param liveUid 主播的uid
     * @param stream  主播的stream
     */
    public static void timeCharge(String liveUid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.timeCharge", HttpConsts.TIME_CHARGE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("liveuid", liveUid)
                .execute(callback);
    }

    /**
     * 观众进入直播间
     */
    public static void enterRoom(String liveUid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.enterRoom", HttpConsts.ENTER_ROOM)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("city", AppConfig.getInstance().getCity())
                .params("liveuid", liveUid)
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 发送弹幕
     */
    public static void sendDanmu(String content, String liveUid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.sendBarrage", HttpConsts.SEND_DANMU)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("stream", stream)
                .params("giftid", "1")
                .params("giftcount", "1")
                .params("content", content)
                .execute(callback);
    }

    /**
     * 获取礼物列表，同时会返回剩余的钱
     */
    public static void getGiftList(HttpCallback callback) {
        HttpClient.getInstance().get("Live.getGiftList", HttpConsts.GET_GIFT_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 获取用户余额
     */
    public static void getCoin(HttpCallback callback) {
        HttpClient.getInstance().get("Live.getCoin", HttpConsts.GET_COIN)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 观众给主播送礼物
     */
    public static void sendGift(String liveUid, String stream, int giftId, String giftCount, HttpCallback callback) {
        HttpClient.getInstance().get("Live.sendGift", HttpConsts.SEND_GIFT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("stream", stream)
                .params("giftid", giftId)
                .params("giftcount", giftCount)
                .execute(callback);
    }


    /**
     * 获取主播印象列表
     */
    public static void getAllImpress(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("User.GetUserLabel", HttpConsts.GET_ALL_IMPRESS)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .execute(callback);
    }

    /**
     * 获取自己收到的主播印象列表
     */
    public static void getMyImpress(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("User.GetMyLabel", HttpConsts.GET_MY_IMPRESS)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 给主播设置印象
     */
    public static void setImpress(String touid, String ImpressIDs, HttpCallback callback) {
        HttpClient.getInstance().get("User.setUserLabel", HttpConsts.SET_IMPRESS)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .params("labels", ImpressIDs)
                .execute(callback);
    }

    /**
     * 直播间点击聊天列表和头像出现的弹窗
     */
    public static void getLiveUser(String touid, String liveUid, HttpCallback callback) {
        HttpClient.getInstance().get("Live.getPop", HttpConsts.GET_LIVE_USER)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("liveuid", liveUid)
                .execute(callback);
    }

    /**
     * 获取当前直播间的管理员列表
     */
    public static void getAdminList(String liveUid, HttpCallback callback) {
        HttpClient.getInstance().get("Live.getAdminList", HttpConsts.GET_ADMIN_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("liveuid", liveUid)
                .execute(callback);
    }

    /**
     * 主播设置或取消直播间的管理员
     */
    public static void setAdmin(String liveUid, String touid, HttpCallback callback) {
        HttpClient.getInstance().get("Live.setAdmin", HttpConsts.SET_ADMIN)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("touid", touid)
                .execute(callback);
    }

    /**
     * 主播或管理员踢人
     */
    public static void kicking(String liveUid, String touid, HttpCallback callback) {
        HttpClient.getInstance().get("Live.kicking", HttpConsts.KICKING)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("touid", touid)
                .execute(callback);
    }


    /**
     * 主播或管理员禁言
     */
    public static void setShutUp(String liveUid, String touid, HttpCallback callback) {
        HttpClient.getInstance().get("Live.setShutUp", HttpConsts.SET_SHUT_UP)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("touid", touid)
                .execute(callback);
    }


    /**
     * 超管关闭直播间或禁用账户
     */
    public static void superCloseRoom(String liveUid, boolean forbidAccount, HttpCallback callback) {
        HttpClient.getInstance().get("Live.superStopRoom", HttpConsts.SUPER_CLOSE_ROOM)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("type", forbidAccount ? 1 : 0)
                .execute(callback);
    }


    /**
     * 举报用户
     */
    public static void setReport(String touid, String content,HttpCallback callback) {
        HttpClient.getInstance().get("Live.setReport", HttpConsts.SET_REPORT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .params("content", content)
                .execute(callback);
    }

    /**
     * 用户个人主页信息
     */
    public static void getUserHome(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("User.getUserHome", HttpConsts.GET_USER_HOME)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .execute(callback);
    }

    /**
     * 拉黑对方， 解除拉黑
     */
    public static void setBlack(String toUid, HttpCallback callback) {
        HttpClient.getInstance().get("User.setBlack", HttpConsts.SET_BLACK)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", toUid)
                .execute(callback);
    }

    /**
     * 主播添加背景音乐时，搜索歌曲
     *
     * @param key      关键字
     * @param callback
     */
    public static void searchMusic(String key, HttpCallback callback) {
        HttpClient.getInstance().get("Livemusic.searchMusic", HttpConsts.SEARCH_MUSIC)
                .params("key", key)
                .execute(callback);
    }

    /**
     * 获取歌曲的地址 和歌词的地址
     */
    public static void getMusicUrl(String musicId, HttpCallback callback) {
        HttpClient.getInstance().get("Livemusic.getDownurl", HttpConsts.GET_MUSIC_URL)
                .params("audio_id", musicId)
                .execute(callback);
    }

    /**
     * 获取 我的收益 可提现金额数
     */
    public static void getProfit(HttpCallback callback) {
        HttpClient.getInstance().get("User.getProfit", HttpConsts.GET_PROFIT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 获取 提现账户列表
     */
    public static void getCashAccountList(HttpCallback callback) {
        HttpClient.getInstance().get("User.GetUserAccountList", HttpConsts.GET_USER_ACCOUNT_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 添加 提现账户
     */
    public static void addCashAccount(String account, String name, String bank, int type, HttpCallback callback) {
        HttpClient.getInstance().get("User.SetUserAccount", HttpConsts.ADD_CASH_ACCOUNT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("account", account)
                .params("name", name)
                .params("account_bank", bank)
                .params("type", type)
                .execute(callback);
    }

    /**
     * 删除 提现账户
     */
    public static void deleteCashAccount(String accountId, HttpCallback callback) {
        HttpClient.getInstance().get("User.delUserAccount", HttpConsts.DEL_CASH_ACCOUNT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("id", accountId)
                .execute(callback);
    }

    /**
     * 提现
     */
    public static void doCash(String votes, String accountId, HttpCallback callback) {
        HttpClient.getInstance().get("User.setCash", HttpConsts.DO_CASH)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("cashvote", votes)//提现的票数
                .params("accountid", accountId)//账号ID
                .execute(callback);
    }

    /**
     * 充值页面，我的钻石
     */
    public static void getBalance(HttpCallback callback) {
        HttpClient.getInstance().get("User.getBalance", HttpConsts.GET_BALANCE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 用支付宝充值 的时候在服务端生成订单号
     *
     * @param money    RMB价格
     * @param changeid 要购买的钻石的id
     * @param coin     要购买的钻石的数量
     * @param callback
     */
    public static void getAliOrder(String money, String changeid, String coin, HttpCallback callback) {
        HttpClient.getInstance().get("Charge.getAliOrder", HttpConsts.GET_ALI_ORDER)
                .params("uid", AppConfig.getInstance().getUid())
                .params("money", money)
                .params("changeid", changeid)
                .params("coin", coin)
                .execute(callback);
    }

    /**
     * 用微信支付充值 的时候在服务端生成订单号
     *
     * @param money    RMB价格
     * @param changeid 要购买的钻石的id
     * @param coin     要购买的钻石的数量
     * @param callback
     */
    public static void getWxOrder(String money, String changeid, String coin, HttpCallback callback) {
        HttpClient.getInstance().get("Charge.getWxOrder", HttpConsts.GET_WX_ORDER)
                .params("uid", AppConfig.getInstance().getUid())
                .params("money", money)
                .params("changeid", changeid)
                .params("coin", coin)
                .execute(callback);
    }

    /**
     * 私信聊天页面用于获取用户信息
     */
    public static void getImUserInfo(String uids, HttpCallback callback) {
        HttpClient.getInstance().get("User.GetUidsInfo", HttpConsts.GET_IM_USER_INFO)
                .params("uid", AppConfig.getInstance().getUid())
                .params("uids", uids)
                .execute(callback);
    }

    /**
     * 判断自己有没有被对方拉黑，聊天的时候用到
     */
    public static void checkBlack(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("User.checkBlack", HttpConsts.CHECK_BLACK)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .execute(callback);
    }

    /**
     * 搜索
     */
    public static void search(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Home.search", HttpConsts.SEARCH)
                .params("uid", AppConfig.getInstance().getUid())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 观众跟主播连麦时，获取自己的流地址
     */
    public static void getLinkMicStream(HttpCallback callback) {
        HttpClient.getInstance().get("Linkmic.RequestLVBAddrForLinkMic", HttpConsts.GET_LINK_MIC_STREAM)
                .params("uid", AppConfig.getInstance().getUid())
                .execute(callback);
    }

    /**
     * 主播连麦成功后，要把这些信息提交给服务器
     *
     * @param touid    连麦用户ID
     * @param pull_url 连麦用户播流地址
     */
    public static void linkMicShowVideo(String touid, String pull_url) {
        HttpClient.getInstance().get("Live.showVideo", HttpConsts.LINK_MIC_SHOW_VIDEO)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("pull_url", pull_url)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {

                    }
                });
    }


    /**
     * 获取当前直播间的用户列表
     */
    public static void getUserList(String liveuid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Live.getUserLists", HttpConsts.GET_USER_LIST)
                .params("liveuid", liveuid)
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 获取直播回放url
     *
     * @param recordId 视频的id
     */
    public static void getAliCdnRecord(String recordId, HttpCallback callback) {
        HttpClient.getInstance().get("User.getAliCdnRecord", HttpConsts.GET_ALI_CDN_RECORD)
                .params("id", recordId)
                .execute(callback);
    }


    /**
     * 用于用户首次登录推荐
     */
    public static void getRecommend(HttpCallback callback) {
        HttpClient.getInstance().get("Home.getRecommend", HttpConsts.GET_RECOMMEND)
                .params("uid", AppConfig.getInstance().getUid())
                .execute(callback);
    }


    /**
     * 用于用户首次登录推荐,关注主播
     */
    public static void recommendFollow(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("Home.attentRecommend", HttpConsts.RECOMMEND_FOLLOW)
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .execute(callback);
    }

    /**
     * 用于用户首次登录设置分销关系
     */
    public static void setDistribut(String code, HttpCallback callback) {
        HttpClient.getInstance().get("User.setDistribut", HttpConsts.SET_DISTRIBUT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("code", code)
                .execute(callback);
    }


    /**
     * 守护商品类型列表
     */
    public static void getGuardBuyList(HttpCallback callback) {
        HttpClient.getInstance().get("Guard.getList", HttpConsts.GET_GUARD_BUY_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 购买守护接口
     */
    public static void buyGuard(String liveUid, String stream, int guardId, HttpCallback callback) {
        HttpClient.getInstance().get("Guard.BuyGuard", HttpConsts.BUY_GUARD)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("stream", stream)
                .params("guardid", guardId)
                .execute(callback);
    }


    /**
     * 查看主播的守护列表
     */
    public static void getGuardList(String liveUid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Guard.GetGuardList", HttpConsts.GET_GUARD_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 获取主播连麦pk列表
     */
    public static void getLivePkList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Livepk.GetLiveList", HttpConsts.GET_LIVE_PK_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 连麦pk搜索主播
     */
    public static void livePkSearchAnchor(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Livepk.Search", HttpConsts.LIVE_PK_SEARCH_ANCHOR)
                .params("uid", AppConfig.getInstance().getUid())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 连麦pk检查对方主播在线状态
     */
    public static void livePkCheckLive(String liveUid, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Livepk.checkLive", HttpConsts.LIVE_PK_CHECK_LIVE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveUid)
                .params("stream", stream)
                .execute(callback);
    }


    /**
     * 直播间发红包
     */
    public static void sendRedPack(String stream, String coin, String count, String title, int type, int sendType, HttpCallback callback) {
        HttpClient.getInstance().get("Red.SendRed", HttpConsts.SEND_RED_PACK)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("coin", coin)
                .params("nums", count)
                .params("des", title)
                .params("type", type)
                .params("type_grant", sendType)
                .execute(callback);
    }

    /**
     * 获取直播间红包列表
     */
    public static void getRedPackList(String stream, HttpCallback callback) {
        String sign = MD5Util.getMD5("stream=" + stream + "&" + SALT);
        HttpClient.getInstance().get("Red.GetRedList", HttpConsts.GET_RED_PACK_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("sign", sign)
                .execute(callback);
    }

    /**
     * 直播间抢红包
     */
    public static void robRedPack(String stream, int redPackId, HttpCallback callback) {
        String uid = AppConfig.getInstance().getUid();
        String sign = MD5Util.getMD5("redid=" + redPackId + "&stream=" + stream + "&uid=" + uid + "&" + SALT);
        HttpClient.getInstance().get("Red.RobRed", HttpConsts.ROB_RED_PACK)
                .params("uid", uid)
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("redid", redPackId)
                .params("sign", sign)
                .execute(callback);
    }


    /**
     * 直播间红包领取详情
     */
    public static void getRedPackResult(String stream, int redPackId, HttpCallback callback) {
        String uid = AppConfig.getInstance().getUid();
        String sign = MD5Util.getMD5("redid=" + redPackId + "&stream=" + stream + "&" + SALT);
        HttpClient.getInstance().get("Red.GetRedRobList", HttpConsts.GET_RED_PACK_RESULT)
                .params("uid", uid)
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("redid", redPackId)
                .params("sign", sign)
                .execute(callback);
    }

    /**
     * 获取系统消息列表
     */
    public static void getSystemMessageList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Message.GetList", HttpConsts.GET_SYSTEM_MESSAGE_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }


    /**
     * 主播设置是否允许观众发起连麦
     */
    public static void setLinkMicEnable(boolean linkMicEnable, HttpCallback callback) {
        HttpClient.getInstance().get("Linkmic.setMic", HttpConsts.SET_LINK_MIC_ENABLE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("ismic", linkMicEnable ? 1 : 0)
                .execute(callback);
    }


    /**
     * 观众检查主播是否允许连麦
     */
    public static void checkLinkMicEnable(String liveUid, HttpCallback callback) {
        HttpClient.getInstance().get("Linkmic.isMic", HttpConsts.CHECK_LINK_MIC_ENABLE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("liveuid", liveUid)
                .execute(callback);
    }

    /**
     * 获取直播间举报内容列表
     */
    public static void getLiveReportList(HttpCallback callback) {
        HttpClient.getInstance().get("Live.getReportClass", HttpConsts.GET_LIVE_REPORT_LIST)
                .execute(callback);
    }

    /**********************
     * 视频
     *****************/

    /**
     * 获取首页视频列表
     */
    public static void getHomeVideoList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetVideoList", HttpConsts.GET_HOME_VIDEO_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 视频点赞
     */
    public static void setVideoLike(String tag, String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.AddLike", tag)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }

    /**
     * 获取视频评论
     */
    public static void getVideoCommentList(String videoid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetComments", HttpConsts.GET_VIDEO_COMMENT_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 评论点赞
     */
    public static void setCommentLike(String commentid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.addCommentLike", HttpConsts.SET_COMMENT_LIKE)
                .params("commentid", commentid)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 发表评论
     */
    public static void setComment(String toUid, String videoId, String content, String commentId, String parentId, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setComment", HttpConsts.SET_COMMENT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", toUid)
                .params("videoid", videoId)
                .params("commentid", commentId)
                .params("parentid", parentId)
                .params("content", content)
                .params("at_info", "")
                .execute(callback);
    }


    /**
     * 获取评论回复
     */
    public static void getCommentReply(String commentid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getReplys", HttpConsts.GET_COMMENT_REPLY)
                .params("commentid", commentid)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取视频音乐分类列表
     */
    public static void getMusicClassList(HttpCallback callback) {
        HttpClient.getInstance().get("Music.classify_list", HttpConsts.GET_MUSIC_CLASS_LIST)
                .execute(callback);
    }

    /**
     * 获取热门视频音乐列表
     */
    public static void getHotMusicList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.hotLists", HttpConsts.GET_HOT_MUSIC_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 音乐收藏
     */
    public static void setMusicCollect(int muiscId, HttpCallback callback) {
        HttpClient.getInstance().get("Music.collectMusic", HttpConsts.SET_MUSIC_COLLECT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("musicid", muiscId)
                .execute(callback);
    }

    /**
     * 音乐收藏列表
     */
    public static void getMusicCollectList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.getCollectMusicLists", HttpConsts.GET_MUSIC_COLLECT_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取具体分类下的音乐列表
     */
    public static void getMusicList(String classId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.music_list", HttpConsts.GET_MUSIC_LIST)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("classify", classId)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 搜索音乐
     */
    public static void videoSearchMusic(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.searchMusic", HttpConsts.VIDEO_SEARCH_MUSIC)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 上传视频，获取七牛云token的接口
     */
    public static void getQiNiuToken(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getQiniuToken", HttpConsts.GET_QI_NIU_TOKEN)
                .execute(callback);
    }


    /**
     * 短视频上传信息
     *
     * @param title   短视频标题
     * @param thumb   短视频封面图url
     * @param href    短视频视频url
     * @param musicId 背景音乐Id
     */
    public static void saveUploadVideoInfo(String title, String thumb, String href, int musicId, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setVideo", HttpConsts.SAVE_UPLOAD_VIDEO_INFO)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("lat", AppConfig.getInstance().getLat())
                .params("lng", AppConfig.getInstance().getLng())
                .params("city", AppConfig.getInstance().getCity())
                .params("title", title)
                .params("thumb", thumb)
                .params("href", href)
                .params("music_id", musicId)
                .execute(callback);
    }

    /**
     * 获取腾讯云储存上传签名
     */
    public static void getTxUploadCredential(StringCallback callback) {
        OkGo.<String>get("http://upload.qq163.iego.cn:8088/cam")
                .execute(callback);
    }

    /**
     * 获取某人发布的视频
     */
    public static void getHomeVideo(String toUid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getHomeVideo", HttpConsts.GET_HOME_VIDEO)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", toUid)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 获取举报内容列表
     */
    public static void getVideoReportList(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getReportContentlist", HttpConsts.GET_VIDEO_REPORT_LIST)
                .execute(callback);
    }



    /**
     * 举报视频接口
     */
    public static void videoReport(String videoId, String reportId, String content, HttpCallback callback) {
        HttpClient.getInstance().get("Video.report", HttpConsts.VIDEO_REPORT)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("type", reportId)
                .params("content", content)
                .execute(callback);
    }

    /**
     * 删除自己的视频
     */
    public static void videoDelete(String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.del", HttpConsts.VIDEO_DELETE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }

    /**
     * 分享视频
     */
    public static void setVideoShare(String videoid, HttpCallback callback) {
        String uid = AppConfig.getInstance().getUid();
        String s = MD5Util.getMD5(uid + "-" + videoid + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.addShare", HttpConsts.SET_VIDEO_SHARE)
                .params("uid", uid)
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .params("random_str", s)
                .execute(callback);
    }


    /**
     * 开始观看视频的时候请求这个接口
     */
    public static void videoWatchStart(String videoUid, String videoId) {
        String uid = AppConfig.getInstance().getUid();
        if (TextUtils.isEmpty(uid) || uid.equals(videoUid)) {
            return;
        }
        HttpUtil.cancel(HttpConsts.VIDEO_WATCH_START);
        String s = MD5Util.getMD5(uid + "-" + videoId + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.addView", HttpConsts.VIDEO_WATCH_START)
                .params("uid", uid)
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("random_str", s)
                .execute(NO_CALLBACK);
    }

    /**
     * 完整观看完视频后请求这个接口
     */
    public static void videoWatchEnd(String videoUid, String videoId) {
        String uid = AppConfig.getInstance().getUid();
        if (TextUtils.isEmpty(uid) || uid.equals(videoUid)) {
            return;
        }
        HttpUtil.cancel(HttpConsts.VIDEO_WATCH_END);
        String s = MD5Util.getMD5(uid + "-" + videoId + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.setConversion", HttpConsts.VIDEO_WATCH_END)
                .params("uid", uid)
                .params("token", AppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("random_str", s)
                .execute(NO_CALLBACK);
    }


    //不做任何操作的HttpCallback
    private static final HttpCallback NO_CALLBACK = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {

        }
    };


    /**********************
     * 游戏
     *****************/

    /**
     * 创建炸金花游戏
     */
    public static void gameJinhuaCreate(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Jinhua", HttpConsts.GAME_JINHUA_CREATE)
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 炸金花游戏下注
     */
    public static void gameJinhuaBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.JinhuaBet", HttpConsts.GAME_JINHUA_BET)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }

    /**
     * 游戏结果出来后，观众获取自己赢到的金额
     */
    public static void gameSettle(String gameid, HttpCallback callback) {
        HttpClient.getInstance().get("Game.settleGame", HttpConsts.GAME_SETTLE)
                .params("uid", AppConfig.getInstance().getUid())
                .params("gameid", gameid)
                .execute(callback);
    }

    /**
     * 创建海盗船长游戏
     */
    public static void gameHaidaoCreate(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Taurus", HttpConsts.GAME_HAIDAO_CREATE)
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 海盗船长游戏下注
     */
    public static void gameHaidaoBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Taurus_Bet", HttpConsts.GAME_HAIDAO_BET)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }

    /**
     * 创建幸运转盘游戏
     */
    public static void gameLuckPanCreate(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Dial", HttpConsts.GAME_LUCK_PAN_CREATE)
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 幸运转盘游戏下注
     */
    public static void gameLuckPanBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Dial_Bet", HttpConsts.GAME_LUCK_PAN_BET)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }

    /**
     * 创建开心牛仔游戏
     */
    public static void gameNiuzaiCreate(String stream, String bankerid, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Cowboy", HttpConsts.GAME_NIUZAI_CREATE)
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("bankerid", bankerid)
                .execute(callback);
    }

    /**
     * 开心牛仔游戏下注
     */
    public static void gameNiuzaiBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Cowboy_Bet", HttpConsts.GAME_NIUZAI_BET)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }

    /**
     * 开心牛仔游戏胜负记录
     */
    public static void gameNiuRecord(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.getGameRecord", HttpConsts.GAME_NIU_RECORD)
                .params("action", "4")
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 开心牛仔庄家流水
     */
    public static void gameNiuBankerWater(String bankerId, String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.getBankerProfit", HttpConsts.GAME_NIU_BANKER_WATER)
                .params("bankerid", bankerId)
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 开心牛仔获取庄家列表,列表第一个为当前庄家
     */
    public static void gameNiuGetBanker(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.getBanker", HttpConsts.GAME_NIU_GET_BANKER)
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 开心牛仔申请上庄
     */
    public static void gameNiuSetBanker(String stream, String deposit, HttpCallback callback) {
        HttpClient.getInstance().get("Game.setBanker", HttpConsts.GAME_NIU_SET_BANKER)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("deposit", deposit)
                .execute(callback);
    }

    /**
     * 开心牛仔申请下庄
     */
    public static void gameNiuQuitBanker(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.quietBanker", HttpConsts.GAME_NIU_QUIT_BANKER)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 创建二八贝游戏
     */
    public static void gameEbbCreate(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Cowry", HttpConsts.GAME_EBB_CREATE)
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 二八贝下注
     */
    public static void gameEbbBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Cowry_Bet", HttpConsts.GAME_EBB_BET)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }


}




