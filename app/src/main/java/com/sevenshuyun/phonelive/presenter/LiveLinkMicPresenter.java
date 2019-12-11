package com.sevenshuyun.phonelive.presenter;

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.LiveActivity;
import com.sevenshuyun.phonelive.bean.LevelBean;
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.ILiveLinkMicViewHolder;
import com.sevenshuyun.phonelive.interfaces.LivePushListener;
import com.sevenshuyun.phonelive.socket.SocketClient;
import com.sevenshuyun.phonelive.socket.SocketLinkMicUtil;
import com.sevenshuyun.phonelive.utils.ClickUtil;
import com.sevenshuyun.phonelive.utils.DialogUitl;
import com.sevenshuyun.phonelive.utils.DpUtil;
import com.sevenshuyun.phonelive.utils.IconUtil;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.ProcessResultUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;
import com.sevenshuyun.phonelive.views.LiveLinkMicPlayViewHolder;
import com.sevenshuyun.phonelive.views.LiveLinkMicPushViewHolder;

/**
 * Created by cxf on 2018/10/25.
 * 观众和主播连麦的逻辑
 */

public class LiveLinkMicPresenter implements View.OnClickListener {

    private Context mContext;
    private View mRoot;
    private SocketClient mSocketClient;
    private boolean mIsAnchor;//是否是主播
    private String mLiveUid;//主播的uid
    private ViewGroup mSmallContainer;
    private TextView mLinkMicTip;
    private TextView mLinkMicWaitText;
    private String mApplyUid;//正在申请连麦的人的uid
    private String mLinkMicUid;//已经连麦的人的uid
    private String mLinkMicName;//已经连麦的人的昵称
    private long mLastApplyLinkMicTime;//观众上次申请连麦的时间
    private boolean mIsLinkMic;//是否已经连麦了
    private boolean mIsLinkMicDialogShow;//观众申请连麦的弹窗是否显示了
    private boolean mAcceptLinkMic;//是否接受连麦
    private String mLinkMicWaitString;
    private int mLinkMicWaitCount;//连麦弹窗等待倒计时
    private static final int LINK_MIC_COUNT_MAX = 10;
    private PopupWindow mLinkMicPopWindow;
    private Handler mHandler;
    private LiveLinkMicPlayViewHolder mLiveLinkMicPlayViewHolder;//连麦播放小窗口
    private LiveLinkMicPushViewHolder mLiveLinkMicPushViewHolder;//连麦推流小窗口
    private boolean mPaused;//是否执行了Activity周期的pause

    public LiveLinkMicPresenter(Context context, ILiveLinkMicViewHolder linkMicViewHolder, boolean isAnchor, View root) {
        mContext = context;
        mRoot = root;
        mIsAnchor = isAnchor;
        mSmallContainer = linkMicViewHolder.getSmallContainer();
        if (!isAnchor && root != null) {
            View btnLinkMic = root.findViewById(R.id.btn_link_mic);
            btnLinkMic.setVisibility(View.VISIBLE);
            btnLinkMic.setOnClickListener(this);
            mLinkMicTip = btnLinkMic.findViewById(R.id.link_mic_tip);
        }
        mLinkMicWaitString = WordUtil.getString(R.string.link_mic_wait);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mLinkMicWaitCount--;
                if (mLinkMicWaitCount > 0) {
                    if (mLinkMicWaitText != null) {
                        mLinkMicWaitText.setText(mLinkMicWaitString + "(" + mLinkMicWaitCount + "s)...");
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(0, 1000);
                        }
                    }
                } else {
                    if (mLinkMicPopWindow != null) {
                        mLinkMicPopWindow.dismiss();
                    }
                }
            }
        };
    }

    public void setSocketClient(SocketClient socketClient) {
        mSocketClient = socketClient;
    }

    public void setLiveUid(String liveUid) {
        mLiveUid = liveUid;
    }


    /**
     * 主播收到观众申请连麦的回调
     */
    public void onAudienceApplyLinkMic(UserBean u) {
        if (!mIsAnchor) {
            return;
        }
        if (u == null) {
            return;
        }
        if (!TextUtils.isEmpty(mApplyUid) && mApplyUid.equals(u.getId())) {
            return;
        }
        if (!mIsLinkMic && !mIsLinkMicDialogShow) {
            mApplyUid = u.getId();
            showLinkMicDialog(u);
        } else {
            SocketLinkMicUtil.anchorBusy(mSocketClient, u.getId());
        }
    }

    /**
     * 观众收到主播同意连麦的回调
     */
    public void onAnchorAcceptLinkMic() {
        if (!mIsAnchor) {
            mLastApplyLinkMicTime = 0;
            ToastUtil.show(R.string.link_mic_anchor_accept);
            mIsLinkMic = true;
            mLinkMicUid = AppConfig.getInstance().getUid();
            if (mLinkMicTip != null) {
                mLinkMicTip.setText(R.string.live_link_mic_3);
            }
            HttpUtil.getLinkMicStream(new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        String pushUrl = obj.getString("pushurl");
                        final String playUrl = obj.getString("playurl");
                        L.e("getLinkMicStream", "pushurl--推流地址--->" + pushUrl);
                        L.e("getLinkMicStream", "playurl--播放地址--->" + playUrl);
                        mLiveLinkMicPushViewHolder = new LiveLinkMicPushViewHolder(mContext, mSmallContainer);
                        mLiveLinkMicPushViewHolder.setLivePushListener(new LivePushListener() {
                            @Override
                            public void onPreviewStart() {
                                //预览成功的回调
                            }

                            @Override
                            public void onPushStart() {//推流成功的回调
                                SocketLinkMicUtil.audienceSendLinkMicUrl(mSocketClient, playUrl);
                            }

                            @Override
                            public void onPushFailed() {//推流失败的回调
                                DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.link_mic_failed_2), null);
                                SocketLinkMicUtil.audienceCloseLinkMic(mSocketClient);
                            }
                        });
                        mLiveLinkMicPushViewHolder.addToParent();
                        mLiveLinkMicPushViewHolder.startPush(pushUrl);
                    }
                }
            });
        }
    }

    /**
     * 观众收到主播拒绝连麦的回调
     */
    public void onAnchorRefuseLinkMic() {
        mLastApplyLinkMicTime = 0;
        ToastUtil.show(R.string.link_mic_refuse);
    }

    /**
     * 所有人收到连麦观众发过来的播流地址的回调
     */
    public void onAudienceSendLinkMicUrl(String uid, String uname, String playUrl) {
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        if (mIsAnchor) {
            if (!uid.equals(mApplyUid)) {
                return;
            }
            HttpUtil.linkMicShowVideo(uid, playUrl);
        }
        mApplyUid = null;
        mLinkMicName = uname;
        onLinkMicPlay(uid, playUrl);
    }

    /**
     * 显示连麦的播放窗口
     */
    public void onLinkMicPlay(String uid, String playUrl) {
        mLinkMicUid = uid;
        mLiveLinkMicPlayViewHolder = new LiveLinkMicPlayViewHolder(mContext, mSmallContainer);
        mLiveLinkMicPlayViewHolder.setOnCloseListener(mIsAnchor ? this : null);
        mLiveLinkMicPlayViewHolder.addToParent();
        mLiveLinkMicPlayViewHolder.play(playUrl);
    }

    /**
     * 关闭连麦
     */
    private void closeLinkMic(String uid, String uname) {
        if (!TextUtils.isEmpty(uid) && uid.equals(mLinkMicUid)) {
            ToastUtil.show(uname + WordUtil.getString(R.string.link_mic_exit));
            if (!mIsAnchor && !TextUtils.isEmpty(mLinkMicUid) && mLinkMicUid.equals(AppConfig.getInstance().getUid())) {//参与连麦的是自己
                if (mLiveLinkMicPushViewHolder != null) {
                    mLiveLinkMicPushViewHolder.release();
                    mLiveLinkMicPushViewHolder.removeFromParent();
                }
                mLiveLinkMicPushViewHolder = null;
                if (mLinkMicTip != null) {
                    mLinkMicTip.setText(R.string.live_link_mic_2);
                }
            } else {
                if (mLiveLinkMicPlayViewHolder != null) {
                    mLiveLinkMicPlayViewHolder.release();
                    mLiveLinkMicPlayViewHolder.removeFromParent();
                }
                mLiveLinkMicPlayViewHolder = null;
            }
            mIsLinkMic = false;
            mLinkMicUid = null;
            mLinkMicName = null;
        }
    }

    /**
     * 所有人收到主播关闭连麦的回调
     */
    public void onAnchorCloseLinkMic(String uid, String uname) {
        closeLinkMic(uid, uname);
    }

    /**
     * 所有人收到已连麦观众关闭连麦的回调
     */
    public void onAudienceCloseLinkMic(String uid, String uname) {
        closeLinkMic(uid, uname);
    }

    /**
     * 观众申请连麦时，收到主播无响应的回调
     */
    public void onAnchorNotResponse() {
        mLastApplyLinkMicTime = 0;
        ToastUtil.show(R.string.link_mic_anchor_not_response);
    }

    /**
     * 观众申请连麦时，收到主播正在忙的回调
     */
    public void onAnchorBusy() {
        mLastApplyLinkMicTime = 0;
        ToastUtil.show(R.string.link_mic_anchor_busy);
    }

    /**
     * 已连麦用户退出直播间的回调
     */
    public void onAudienceLinkMicExitRoom(String touid) {

    }

    /**
     * 观众退出直播间回调
     */
    public void onAudienceLeaveRoom(UserBean bean) {
        if (bean != null) {
            String uid = bean.getId();
            if (!TextUtils.isEmpty(uid)) {
                if (uid.equals(mApplyUid)) {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    if (mLinkMicPopWindow != null) {
                        mLinkMicPopWindow.dismiss();
                    }
                }
                if (uid.equals(mLinkMicUid)) {
                    closeLinkMic(uid, bean.getUserNiceName());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtil.canClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_link_mic://观众申请连麦
                onLinkMicBtnClick();
                break;
            case R.id.btn_refuse://主播拒绝连麦
                anchorRefuseLinkMicApply();
                break;
            case R.id.btn_accept://主播接受连麦
                anchorAcceptLinkMicApply();
                break;
            case R.id.btn_close_link_mic://主播断开 已连麦观众 的连麦
                anchorCloseLinkMic();
                break;
        }
    }

    private void onLinkMicBtnClick() {
        if (((LiveActivity) mContext).isGamePlaying()) {
            ToastUtil.show(R.string.live_game_cannot_link_mic);
            return;
        }
        if (((LiveActivity) mContext).isLinkMicAnchor()) {
            ToastUtil.show(R.string.live_link_mic_cannot_link);
            return;
        }
        if (mIsLinkMic) {
            SocketLinkMicUtil.audienceCloseLinkMic(mSocketClient);
        } else {
            checkLinkMicEnable();
        }
    }

    /**
     * 观众检查主播是否允许连麦
     */
    private void checkLinkMicEnable() {
        HttpUtil.checkLinkMicEnable(mLiveUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    audienceApplyLinkMic();
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 观众发起连麦请求
     */
    private void audienceApplyLinkMic() {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastApplyLinkMicTime < 11000) {//时间间隔11秒
            ToastUtil.show(R.string.link_mic_apply_waiting);
        } else {
            mLastApplyLinkMicTime = curTime;
            //请求权限
            ProcessResultUtil processResultUtil = ((LiveActivity) mContext).getProcessImageUtil();
            if (processResultUtil == null) {
                return;
            }
            processResultUtil.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            }, new Runnable() {
                @Override
                public void run() {
                    SocketLinkMicUtil.audienceApplyLinkMic(mSocketClient);
                    ToastUtil.show(R.string.link_mic_apply);
                }
            });
        }
    }


    /**
     * 主播显示连麦的弹窗
     */
    private void showLinkMicDialog(UserBean u) {
        mIsLinkMicDialogShow = true;
        mAcceptLinkMic = false;
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_link_mic_wait, null);
        ImageView avatar = (ImageView) v.findViewById(R.id.avatar);
        TextView name = (TextView) v.findViewById(R.id.name);
        ImageView sex = (ImageView) v.findViewById(R.id.sex);
        ImageView level = (ImageView) v.findViewById(R.id.level);
        mLinkMicWaitText = v.findViewById(R.id.wait_text);
        v.findViewById(R.id.btn_refuse).setOnClickListener(this);
        v.findViewById(R.id.btn_accept).setOnClickListener(this);
        ImgLoader.display(u.getAvatar(), avatar);
        name.setText(u.getUserNiceName());
        sex.setImageResource(IconUtil.getSexIcon(u.getSex()));
        LevelBean levelBean = AppConfig.getInstance().getLevel(u.getLevel());
        if (levelBean != null) {
            ImgLoader.display(levelBean.getThumb(), level);
        }
        mLinkMicWaitCount = LINK_MIC_COUNT_MAX;
        mLinkMicWaitText.setText(mLinkMicWaitString + "(" + mLinkMicWaitCount + ")...");
        mLinkMicPopWindow = new PopupWindow(v, DpUtil.dp2px(280), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mLinkMicPopWindow.setBackgroundDrawable(new ColorDrawable());
        mLinkMicPopWindow.setOutsideTouchable(true);
        mLinkMicPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (mAcceptLinkMic) {
                    if (((LiveActivity) mContext).isGamePlaying()) {
                        ToastUtil.show(R.string.live_game_cannot_link_mic);
                        SocketLinkMicUtil.anchorRefuseLinkMic(mSocketClient, mApplyUid);
                        return;
                    }
                    if (((LiveActivity) mContext).isLinkMicAnchor()) {
                        ToastUtil.show(R.string.live_link_mic_cannot_link_2);
                        return;
                    }
                    SocketLinkMicUtil.anchorAcceptLinkMic(mSocketClient, mApplyUid);
                    mIsLinkMic = true;
                } else {
                    if (mLinkMicWaitCount == 0) {
                        SocketLinkMicUtil.anchorNotResponse(mSocketClient, mApplyUid);
                    } else {
                        SocketLinkMicUtil.anchorRefuseLinkMic(mSocketClient, mApplyUid);
                    }
                    mApplyUid = null;
                }
                mIsLinkMicDialogShow = false;
                mLinkMicWaitText = null;
                mLinkMicPopWindow = null;
            }
        });
        mLinkMicPopWindow.showAtLocation(mRoot, Gravity.CENTER, 0, 0);
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    /**
     * 主播拒绝连麦申请
     */
    private void anchorRefuseLinkMicApply() {
        if (mLinkMicPopWindow != null) {
            mLinkMicPopWindow.dismiss();
        }
    }

    /**
     * 主播接受连麦申请
     */
    private void anchorAcceptLinkMicApply() {
        mAcceptLinkMic = true;
        if (mLinkMicPopWindow != null) {
            mLinkMicPopWindow.dismiss();
        }
    }

    /**
     * 主播断开 已连麦观众 的连麦
     */
    private void anchorCloseLinkMic() {
        SocketLinkMicUtil.anchorCloseLinkMic(mSocketClient, mLinkMicUid, mLinkMicName);
    }

    public void release() {
        HttpUtil.cancel(HttpConsts.GET_LINK_MIC_STREAM);
        HttpUtil.cancel(HttpConsts.LINK_MIC_SHOW_VIDEO);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        mSocketClient = null;
        if (mLiveLinkMicPushViewHolder != null) {
            mLiveLinkMicPushViewHolder.release();
        }
        if (mLiveLinkMicPlayViewHolder != null) {
            mLiveLinkMicPlayViewHolder.release();
        }
        mLiveLinkMicPushViewHolder = null;
        mLiveLinkMicPlayViewHolder = null;
    }

    public void pause() {
        mPaused = true;
        if (mLiveLinkMicPushViewHolder != null) {
            mLiveLinkMicPushViewHolder.pause();
        }
        if (mLiveLinkMicPlayViewHolder != null) {
            mLiveLinkMicPlayViewHolder.pause();
        }
    }

    public void resume() {
        if (mPaused) {
            mPaused = false;
            if (mLiveLinkMicPushViewHolder != null) {
                mLiveLinkMicPushViewHolder.resume();
            }
            if (mLiveLinkMicPlayViewHolder != null) {
                mLiveLinkMicPlayViewHolder.resume();
            }
        }
    }

    public boolean isLinkMic() {
        return mIsLinkMic;
    }


    public void clearData() {
        HttpUtil.cancel(HttpConsts.CHECK_LINK_MIC_ENABLE);
        mIsLinkMic = false;
        mIsLinkMicDialogShow = false;
        mAcceptLinkMic = false;
        mLastApplyLinkMicTime = 0;
        mApplyUid = null;
        mLinkMicUid = null;
        mLinkMicName = null;
        mLinkMicPopWindow = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mLiveLinkMicPlayViewHolder != null) {
            mLiveLinkMicPlayViewHolder.release();
            mLiveLinkMicPlayViewHolder.removeFromParent();
        }
        mLiveLinkMicPlayViewHolder = null;
        if (mLiveLinkMicPushViewHolder != null) {
            mLiveLinkMicPushViewHolder.release();
            mLiveLinkMicPushViewHolder.removeFromParent();
        }
        mLiveLinkMicPushViewHolder = null;
    }

}
