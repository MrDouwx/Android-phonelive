package com.sevenshuyun.phonelive.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.LiveActivity;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.utils.DpUtil;
import com.sevenshuyun.phonelive.utils.StringUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

/**
 * Created by cxf on 2018/11/19.
 * 发红包的弹窗
 */

public class LiveRedPackSendDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private View mGroupPsq;
    private View mGroupPj;
    private EditText mEditCoinPsq;//拼手气钻石数量
    private EditText mEditCountPsq;//拼手气红包数量
    private EditText mEditCoinPj;//平均钻石数量
    private EditText mEditCountPj;//平均红包数量
    private EditText mEditTitle;//红包标题
    private TextView mBtnSend;//发送按钮
    private TextView mCoinName1;
    private TextView mCoinName2;
    private CheckBox mCheckBox;
    private int mRedPackType;
    private String mStream;
    private String mSendRedPackString;
    private String mZuanString;
    private long mCoinPsq = 100;//拼手气钻石数量
    private long mCountPsq = 10;//拼手气红包数量
    private long mCoinPj = 1;//平均钻石数量
    private long mCountPj = 100;//平均红包数量

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_red_pack_send;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(300);
        params.height = DpUtil.dp2px(380);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }


    public void setStream(String stream) {
        mStream = stream;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (TextUtils.isEmpty(mStream)) {
            return;
        }
        mSendRedPackString = WordUtil.getString(R.string.red_pack_6) + " ";
        mCoinName1 = mRootView.findViewById(R.id.coin_name_1);
        mCoinName2 = mRootView.findViewById(R.id.coin_name_2);
        mZuanString = WordUtil.getString(R.string.red_pack_zuan);
        String coinName = AppConfig.getInstance().getCoinName();
        if (!TextUtils.isEmpty(coinName) && !coinName.startsWith(mZuanString)) {
            mZuanString = coinName;
        }
        mCoinName1.setText(mZuanString);
        mCoinName2.setText(mZuanString);
        mGroupPsq = mRootView.findViewById(R.id.group_psq);
        mGroupPj = mRootView.findViewById(R.id.group_pj);
        mEditCoinPsq = mRootView.findViewById(R.id.edit_coin_psq);
        mEditCountPsq = mRootView.findViewById(R.id.edit_count_psq);
        mEditCoinPj = mRootView.findViewById(R.id.edit_coin_pj);
        mEditCountPj = mRootView.findViewById(R.id.edit_count_pj);

        mEditCoinPsq.setText(String.valueOf(mCoinPsq));
        mEditCountPsq.setText(String.valueOf(mCountPsq));
        mEditCoinPj.setText(String.valueOf(mCoinPj));
        mEditCountPj.setText(String.valueOf(mCountPj));

        mEditCoinPsq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String c = s.toString();
                if (TextUtils.isEmpty(c)) {
                    mCoinPsq = 0;
                } else {
                    mCoinPsq = Long.parseLong(c);
                }
                mBtnSend.setText(mSendRedPackString + StringUtil.toWan3(mCoinPsq) + mZuanString);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditCoinPj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String c = s.toString();
                if (TextUtils.isEmpty(c)) {
                    mCoinPj = 0;
                } else {
                    mCoinPj = Long.parseLong(c);
                }
                mBtnSend.setText(mSendRedPackString + StringUtil.toWan3(mCoinPj * mCountPj) + mZuanString);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditCountPj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String c = s.toString();
                if (TextUtils.isEmpty(c)) {
                    mCountPj = 0;
                } else {
                    mCountPj = Long.parseLong(c);
                }
                mBtnSend.setText(mSendRedPackString + StringUtil.toWan3(mCoinPj * mCountPj) + mZuanString);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRootView.findViewById(R.id.btn_psq).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_pj).setOnClickListener(this);
        mBtnSend = mRootView.findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mBtnSend.setText(mSendRedPackString + StringUtil.toWan3(mCoinPsq) + mZuanString);
        mEditTitle = mRootView.findViewById(R.id.edit_title);
        mCheckBox = mRootView.findViewById(R.id.checkbox);
        mRedPackType = Constants.RED_PACK_TYPE_SHOU_QI;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_psq://拼手气
                mRedPackType = Constants.RED_PACK_TYPE_SHOU_QI;
                if (mGroupPsq != null && mGroupPsq.getVisibility() != View.VISIBLE) {
                    mGroupPsq.setVisibility(View.VISIBLE);
                }
                if (mGroupPj != null && mGroupPj.getVisibility() == View.VISIBLE) {
                    mGroupPj.setVisibility(View.INVISIBLE);
                }
                mBtnSend.setText(mSendRedPackString + StringUtil.toWan3(mCoinPsq) + mZuanString);
                break;
            case R.id.btn_pj://平均红包
                mRedPackType = Constants.RED_PACK_TYPE_AVERAGE;
                if (mGroupPj != null && mGroupPj.getVisibility() != View.VISIBLE) {
                    mGroupPj.setVisibility(View.VISIBLE);
                }
                if (mGroupPsq != null && mGroupPsq.getVisibility() == View.VISIBLE) {
                    mGroupPsq.setVisibility(View.INVISIBLE);
                }
                mBtnSend.setText(mSendRedPackString + StringUtil.toWan3(mCoinPj * mCountPj) + mZuanString);
                break;
            case R.id.btn_send://发红包按钮
                sendRedPack();
                break;
        }
    }

    /**
     * 发红包
     */
    private void sendRedPack() {
        String coin = null;
        String count = null;
        if (mRedPackType == Constants.RED_PACK_TYPE_SHOU_QI) {
            coin = mEditCoinPsq.getText().toString().trim();
            count = mEditCountPsq.getText().toString().trim();
        } else {
            coin = mEditCoinPj.getText().toString().trim();
            count = mEditCountPj.getText().toString().trim();
        }
        if (TextUtils.isEmpty(coin)) {
            ToastUtil.show(R.string.red_pack_7);
            return;
        }
        if (TextUtils.isEmpty(count)) {
            ToastUtil.show(R.string.red_pack_8);
            return;
        }
        String title = mEditTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            title = mEditTitle.getHint().toString().trim();
        }
        int sendType = mCheckBox.isChecked() ? Constants.RED_PACK_SEND_TIME_NORMAL : Constants.RED_PACK_SEND_TIME_DELAY;
        HttpUtil.sendRedPack(mStream, coin, count, title, mRedPackType, sendType, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    dismiss();
                    ((LiveActivity) mContext).sendRedPackMessage();
                }
                ToastUtil.show(msg);
            }
        });
    }

    @Override
    public void onDestroy() {
        HttpUtil.cancel(HttpConsts.SEND_RED_PACK);
        super.onDestroy();
    }
}
