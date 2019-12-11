package com.sevenshuyun.phonelive.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opensource.svgaplayer.SVGAImageView;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.LiveRoomScrollAdapter;
import com.sevenshuyun.phonelive.bean.LiveBean;
import com.sevenshuyun.phonelive.bean.LiveGuardInfo;
import com.sevenshuyun.phonelive.bean.LiveUserGiftBean;
import com.sevenshuyun.phonelive.custom.MyViewPager;
import com.sevenshuyun.phonelive.dialog.LiveGiftDialogFragment;
import com.sevenshuyun.phonelive.game.GamePresenter;
import com.sevenshuyun.phonelive.game.bean.GameParam;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.presenter.LiveLinkMicAnchorPresenter;
import com.sevenshuyun.phonelive.presenter.LiveLinkMicPkPresenter;
import com.sevenshuyun.phonelive.presenter.LiveLinkMicPresenter;
import com.sevenshuyun.phonelive.presenter.LiveRoomCheckLivePresenter;
import com.sevenshuyun.phonelive.socket.SocketChatUtil;
import com.sevenshuyun.phonelive.socket.SocketClient;
import com.sevenshuyun.phonelive.utils.DialogUitl;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.LiveStorge;
import com.sevenshuyun.phonelive.utils.RandomUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;
import com.sevenshuyun.phonelive.views.LiveAudienceViewHolder;
import com.sevenshuyun.phonelive.views.LiveEndViewHolder;
import com.sevenshuyun.phonelive.views.LiveKsyPlayViewHolder;
import com.sevenshuyun.phonelive.views.LiveRoomPlayViewHolder;
import com.sevenshuyun.phonelive.views.LiveRoomViewHolder;
import com.sevenshuyun.phonelive.views.LiveTxPlayViewHolder;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/10.
 */

public class LiveAudienceActivity extends LiveActivity {

    private static final String TAG = "LiveAudienceActivity";

    public static void forward(Context context, LiveBean liveBean, int liveType, int liveTypeVal, String key, int position) {
        Intent intent = new Intent(context, LiveAudienceActivity.class);
        intent.putExtra(Constants.LIVE_BEAN, liveBean);
        intent.putExtra(Constants.LIVE_TYPE, liveType);
        intent.putExtra(Constants.LIVE_TYPE_VAL, liveTypeVal);
        intent.putExtra(Constants.LIVE_KEY, key);
        intent.putExtra(Constants.LIVE_POSITION, position);
        context.startActivity(intent);
    }

    private String mKey;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private LiveRoomScrollAdapter mRoomScrollAdapter;
    private View mMainContentView;
    private MyViewPager mViewPager;
    private ViewGroup mSecondPage;//默认显示第二页
    private FrameLayout mContainerWrap;
    private LiveRoomPlayViewHolder mLivePlayViewHolder;
    private LiveAudienceViewHolder mLiveAudienceViewHolder;
    private boolean mLighted;
    private long mLastLightClickTime;
    private boolean mEnd;
    private boolean mCoinNotEnough;//余额不足
    private LiveRoomCheckLivePresenter mCheckLivePresenter;

    @Override
    public <T extends View> T findViewById(@IdRes int id) {
        if (AppConfig.LIVE_ROOM_SCROLL) {
            if (mMainContentView != null) {
                return mMainContentView.findViewById(id);
            }
        }
        return super.findViewById(id);
    }

    @Override
    protected int getLayoutId() {
        if (AppConfig.LIVE_ROOM_SCROLL) {
            return R.layout.activity_live_audience_2;
        }
        return R.layout.activity_live_audience;
    }

    public void setScrollFrozen(boolean frozen) {
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutFrozen(frozen);
        }
    }

    @Override
    protected void main() {
        if (AppConfig.LIVE_ROOM_SCROLL) {
            mRecyclerView = super.findViewById(R.id.recyclerView);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            mMainContentView = LayoutInflater.from(mContext).inflate(R.layout.activity_live_audience, null, false);
        }
        super.main();
        if (AppConfig.LIVE_ROOM_SCROLL) {
            //腾讯视频播放器
            mLivePlayViewHolder = new LiveTxPlayViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
        } else {
            //金山云播放器
            mLivePlayViewHolder = new LiveKsyPlayViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
        }
        mLivePlayViewHolder.addToParent();
        addLifeCycleListener(mLivePlayViewHolder.getLifeCycleListener());
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        mSecondPage = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.view_audience_page, mViewPager, false);
        mContainerWrap = mSecondPage.findViewById(R.id.container_wrap);
        mContainer = mSecondPage.findViewById(R.id.container);
        mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) mSecondPage.findViewById(R.id.gift_gif), (SVGAImageView) mSecondPage.findViewById(R.id.gift_svga));
        mLiveRoomViewHolder.addToParent();
        addLifeCycleListener(mLiveRoomViewHolder.getLifeCycleListener());
        mLiveAudienceViewHolder = new LiveAudienceViewHolder(mContext, mContainer);
        mLiveAudienceViewHolder.addToParent();
        mLiveAudienceViewHolder.setUnReadCount(getImUnReadCount());
        mLiveBottomViewHolder = mLiveAudienceViewHolder;
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    View view = new View(mContext);
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    container.addView(view);
                    return view;
                } else {
                    container.addView(mSecondPage);
                    return mSecondPage;
                }
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            }
        });
        mViewPager.setCurrentItem(1);
        mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePlayViewHolder, false, mLiveAudienceViewHolder.getContentView());
        mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePlayViewHolder, false, null);
        mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePlayViewHolder, false, null);
        Intent intent = getIntent();
        if (AppConfig.LIVE_ROOM_SCROLL) {
            mKey = intent.getStringExtra(Constants.LIVE_KEY);
            mPosition = intent.getIntExtra(Constants.LIVE_POSITION, 0);
            List<LiveBean> list = LiveStorge.getInstance().get(mKey);
            mRoomScrollAdapter = new LiveRoomScrollAdapter(mContext, list, mPosition);
            mRoomScrollAdapter.setActionListener(new LiveRoomScrollAdapter.ActionListener() {
                @Override
                public void onPageSelected(LiveBean liveBean, ViewGroup container, boolean first) {
                    L.e(TAG, "onPageSelected----->" + liveBean);
                    if (mMainContentView != null && container != null) {
                        ViewParent parent = mMainContentView.getParent();
                        if (parent != null) {
                            ViewGroup viewGroup = (ViewGroup) parent;
                            if (viewGroup != container) {
                                viewGroup.removeView(mMainContentView);
                                container.addView(mMainContentView);
                            }
                        } else {
                            container.addView(mMainContentView);
                        }
                    }
                    if (!first) {
                        checkLive(liveBean);
                    }
                }

                @Override
                public void onPageOutWindow(String liveUid) {
                    L.e(TAG, "onPageOutWindow----->" + liveUid);
                    if (TextUtils.isEmpty(mLiveUid) || mLiveUid.equals(liveUid)) {
                        HttpUtil.cancel(HttpConsts.CHECK_LIVE);
                        HttpUtil.cancel(HttpConsts.ENTER_ROOM);
                        HttpUtil.cancel(HttpConsts.ROOM_CHARGE);
                        clearRoomData();
                    }
                }
            });
            mRecyclerView.setAdapter(mRoomScrollAdapter);
        }
        mLiveType = intent.getIntExtra(Constants.LIVE_TYPE, Constants.LIVE_TYPE_NORMAL);
        mLiveTypeVal = intent.getIntExtra(Constants.LIVE_TYPE_VAL, 0);
        LiveBean liveBean = intent.getParcelableExtra(Constants.LIVE_BEAN);
        setLiveRoomData(liveBean);
        enterRoom();
    }


    private void setLiveRoomData(LiveBean liveBean) {
        mLiveBean = liveBean;
        mLiveUid = liveBean.getUid();
        mStream = liveBean.getStream();
        mLivePlayViewHolder.setCover(liveBean.getThumb());
        mLivePlayViewHolder.play(liveBean.getPull());
        mLiveAudienceViewHolder.setLiveInfo(mLiveUid, mStream);
        mLiveRoomViewHolder.setAvatar(liveBean.getAvatar());
        mLiveRoomViewHolder.setAnchorLevel(liveBean.getLevelAnchor());
        mLiveRoomViewHolder.setName(liveBean.getUserNiceName());
        mLiveRoomViewHolder.setRoomNum(liveBean.getLiangNameTip());
        mLiveLinkMicPkPresenter.setLiveUid(mLiveUid);
        mLiveLinkMicPresenter.setLiveUid(mLiveUid);
    }

    private void clearRoomData() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        mSocketClient = null;
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.stopPlay();
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.clearData();
        }
        if (mGamePresenter != null) {
            mGamePresenter.clearGame();
        }
        if (mLiveEndViewHolder != null) {
            mLiveEndViewHolder.removeFromParent();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.clearData();
        }
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.clearData();
        }
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.clearData();
        }
    }

    private void checkLive(LiveBean bean) {
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new LiveRoomCheckLivePresenter(mContext, new LiveRoomCheckLivePresenter.ActionListener() {
                @Override
                public void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal) {
                    if (liveBean == null) {
                        return;
                    }
                    setLiveRoomData(liveBean);
                    mLiveType = liveType;
                    mLiveTypeVal = liveTypeVal;
                    if (mRoomScrollAdapter != null) {
                        mRoomScrollAdapter.hideCover();
                    }
                    enterRoom();
                }
            });
        }
        mCheckLivePresenter.checkLive(bean);
    }


    private void enterRoom() {
        HttpUtil.enterRoom(mLiveUid, mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mDanmuPrice = obj.getString("barrage_fee");
                    mShutTime = obj.getString("shut_time");
                    mSocketUserType = obj.getIntValue("usertype");
                    //连接socket
                    mSocketClient = new SocketClient(obj.getString("chatserver"), LiveAudienceActivity.this);
                    if (mLiveLinkMicPresenter != null) {
                        mLiveLinkMicPresenter.setSocketClient(mSocketClient);
                    }
                    mSocketClient.connect(mLiveUid, mStream);
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.setLiveInfo(mLiveUid, mStream, obj.getIntValue("userlist_time") * 1000);
                        mLiveRoomViewHolder.setVotes(obj.getString("votestotal"));
                        mLiveRoomViewHolder.setAttention(obj.getIntValue("isattention"));
                        List<LiveUserGiftBean> list = JSON.parseArray(obj.getString("userlists"), LiveUserGiftBean.class);
                        mLiveRoomViewHolder.setUserList(list);
                        mLiveRoomViewHolder.startRefreshUserList();
                        if (mLiveType == Constants.LIVE_TYPE_TIME) {//计时收费
                            mLiveRoomViewHolder.startRequestTimeCharge();
                        }
                    }
                    //判断是否有连麦，要显示连麦窗口
                    String linkMicUid = obj.getString("linkmic_uid");
                    String linkMicPull = obj.getString("linkmic_pull");
                    if (!TextUtils.isEmpty(linkMicUid) && !"0".equals(linkMicUid) && !TextUtils.isEmpty(linkMicPull)) {
                        if (mLiveLinkMicPresenter != null) {
                            mLiveLinkMicPresenter.onLinkMicPlay(linkMicUid, linkMicPull);
                        }
                    }
                    //判断是否有主播连麦
                    JSONObject pkInfo = JSON.parseObject(obj.getString("pkinfo"));
                    if (pkInfo != null) {
                        String pkUid = pkInfo.getString("pkuid");
                        if (!TextUtils.isEmpty(pkUid) && !"0".equals(pkUid)) {
                            String pkPull = pkInfo.getString("pkpull");
                            if (!TextUtils.isEmpty(pkPull)) {
                                if (mLiveLinkMicAnchorPresenter != null) {
                                    mLiveLinkMicAnchorPresenter.onLinkMicAnchorPlayUrl(pkUid, pkPull);
                                }
                            }
                            if (pkInfo.getIntValue("ifpk") == 1) {//pk开始了
                                mLiveLinkMicPkPresenter.onEnterRoomPkStart(pkUid, pkInfo.getLongValue("pk_gift_liveuid"), pkInfo.getLongValue("pk_gift_pkuid"), pkInfo.getIntValue("pk_time") * 1000);
                            }
                        }
                    }

                    //守护相关
                    mLiveGuardInfo = new LiveGuardInfo();
                    int guardNum = obj.getIntValue("guard_nums");
                    mLiveGuardInfo.setGuardNum(guardNum);
                    JSONObject guardObj = obj.getJSONObject("guard");
                    if (guardObj != null) {
                        mLiveGuardInfo.setMyGuardType(guardObj.getIntValue("type"));
                        mLiveGuardInfo.setMyGuardEndTime(guardObj.getString("endtime"));
                    }
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.setGuardNum(guardNum);
                        //红包相关
                        mLiveRoomViewHolder.setRedPackBtnVisible(obj.getIntValue("isred") == 1);
                    }

                    //游戏相关
                    if (AppConfig.GAME_ENABLE) {
                        GameParam param = new GameParam();
                        param.setContext(mContext);
                        param.setParentView(mContainerWrap);
                        param.setTopView(mContainer);
                        param.setInnerContainer(mLiveRoomViewHolder.getInnerContainer());
                        param.setSocketClient(mSocketClient);
                        param.setLiveUid(mLiveUid);
                        param.setStream(mStream);
                        param.setAnchor(false);
                        param.setCoinName(mCoinName);
                        param.setObj(obj);
                        if (mGamePresenter == null) {
                            mGamePresenter = new GamePresenter();
                        }
                        mGamePresenter.setGameParam(param);
                    }
                }
            }
        });
    }

    /**
     * 打开礼物窗口
     */
    public void openGiftWindow() {
        if (TextUtils.isEmpty(mLiveUid) || TextUtils.isEmpty(mStream)) {
            return;
        }
        LiveGiftDialogFragment fragment = new LiveGiftDialogFragment();
        fragment.setLiveGuardInfo(mLiveGuardInfo);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putString(Constants.LIVE_STREAM, mStream);
        fragment.setArguments(bundle);
        fragment.show(((LiveAudienceActivity) mContext).getSupportFragmentManager(), "LiveGiftDialogFragment");
    }

    /**
     * 结束观看
     */
    private void endPlay() {
        HttpUtil.cancel(HttpConsts.ENTER_ROOM);
        if (mEnd) {
            return;
        }
        mEnd = true;
        HttpUtil.cancel(HttpConsts.ENTER_ROOM);
        //断开socket
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        mSocketClient = null;
        //结束播放
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.release();
        }
        mLivePlayViewHolder = null;
        release();
    }

    @Override
    protected void release() {
        super.release();
        if (mRoomScrollAdapter != null) {
            mRoomScrollAdapter.setActionListener(null);
        }
        mRoomScrollAdapter = null;
    }

    /**
     * 观众收到直播结束消息
     */
    @Override
    public void onLiveEnd() {
        super.onLiveEnd();
        if (!AppConfig.LIVE_ROOM_SCROLL) {
            if (mViewPager != null) {
                if (mViewPager.getCurrentItem() != 1) {
                    mViewPager.setCurrentItem(1, false);
                }
                mViewPager.setCanScroll(false);
            }
            endPlay();
        }
        if (mLiveEndViewHolder == null) {
            mLiveEndViewHolder = new LiveEndViewHolder(mContext, mSecondPage);
            addLifeCycleListener(mLiveEndViewHolder.getLifeCycleListener());
            mLiveEndViewHolder.addToParent();
        }
        mLiveEndViewHolder.showData(mLiveBean, mStream);
    }


    /**
     * 观众收到踢人消息
     */
    @Override
    public void onKick(String touid) {
        if (!TextUtils.isEmpty(touid) && touid.equals(AppConfig.getInstance().getUid())) {//被踢的是自己
            exitLiveRoom();
            ToastUtil.show(WordUtil.getString(R.string.live_kicked_2));
        }
    }

    /**
     * 观众收到禁言消息
     */
    @Override
    public void onShutUp(String touid, String content) {
        if (!TextUtils.isEmpty(touid) && touid.equals(AppConfig.getInstance().getUid())) {
            DialogUitl.showSimpleDialog(mContext, content, null);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mEnd && !canBackPressed()) {
            return;
        }
        exitLiveRoom();
    }

    /**
     * 退出直播间
     */
    public void exitLiveRoom() {
        endPlay();
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.e("LiveAudienceActivity-------onDestroy------->");
    }

    /**
     * 点亮
     */
    public void light() {
        if (!mLighted) {
            mLighted = true;
            int guardType = mLiveGuardInfo != null ? mLiveGuardInfo.getMyGuardType() : Constants.GUARD_TYPE_NONE;
            SocketChatUtil.sendLightMessage(mSocketClient, 1 + RandomUtil.nextInt(6), guardType);
        } else {
            long cutTime = System.currentTimeMillis();
            if (cutTime - mLastLightClickTime < 5000) {
                if (mLiveRoomViewHolder != null) {
                    mLiveRoomViewHolder.playLightAnim();
                }
            } else {
                mLastLightClickTime = cutTime;
                SocketChatUtil.sendFloatHeart(mSocketClient);
            }
        }
    }


    /**
     * 计时收费更新主播映票数
     */
    public void roomChargeUpdateVotes() {
        sendUpdateVotesMessage(mLiveTypeVal);
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.pausePlay();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlay() {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.resumePlay();
        }
    }

    /**
     * 充值成功
     */
    public void onChargeSuccess() {
        if (mLiveType == Constants.LIVE_TYPE_TIME) {
            if (mCoinNotEnough) {
                mCoinNotEnough = false;
                HttpUtil.roomCharge(mLiveUid, mStream, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            roomChargeUpdateVotes();
                            if (mLiveRoomViewHolder != null) {
                                resumePlay();
                                mLiveRoomViewHolder.startRequestTimeCharge();
                            }
                        } else {
                            if (code == 1008) {//余额不足
                                mCoinNotEnough = true;
                                DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.live_coin_not_enough), false,
                                        new DialogUitl.SimpleCallback2() {
                                            @Override
                                            public void onConfirmClick(Dialog dialog, String content) {
                                                MyCoinActivity.forward(mContext);
                                            }

                                            @Override
                                            public void onCancelClick() {
                                                exitLiveRoom();
                                            }
                                        });
                            }
                        }
                    }
                });
            }
        }
    }

    public void setCoinNotEnough(boolean coinNotEnough) {
        mCoinNotEnough = coinNotEnough;
    }

}
