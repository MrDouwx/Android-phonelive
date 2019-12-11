package com.sevenshuyun.phonelive.activity;

import android.content.Intent;
import android.view.View;
import android.widget.RadioButton;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

/**
 * Created by cxf on 2018/9/29.
 * 设置性别
 */

public class EditSexActivity extends AbsActivity implements View.OnClickListener {

    private RadioButton mBtnMale;
    private RadioButton mBtnFeMale;
    private int mSex;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_sex;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.edit_profile_sex));
        mBtnMale = (RadioButton)findViewById(R.id.btn_male);
        mBtnFeMale = (RadioButton)findViewById(R.id.btn_female);
        mBtnMale.setOnClickListener(this);
        mBtnFeMale.setOnClickListener(this);
        mSex = getIntent().getIntExtra(Constants.SEX, Constants.SEX_MALE);
        if (mSex == Constants.SEX_FEMALE) {
            mBtnFeMale.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        if(!canClick()){
            return;
        }
        switch (v.getId()) {
            case R.id.btn_male:
                setSex(Constants.SEX_MALE);
                break;
            case R.id.btn_female:
                setSex(Constants.SEX_FEMALE);
                break;
        }
    }

    private void setSex(int sex) {
        if (mSex == sex) {
            return;
        }
        mSex = sex;
        HttpUtil.updateFields("{\"sex\":\"" + sex + "\"}", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    ToastUtil.show(obj.getString("msg"));
                    if (mSex == Constants.SEX_MALE) {
                        mBtnMale.setChecked(true);
                    } else {
                        mBtnFeMale.setChecked(true);
                    }
                    UserBean u = AppConfig.getInstance().getUserBean();
                    if (u != null) {
                        u.setSex(mSex);
                    }
                    Intent intent = getIntent();
                    intent.putExtra(Constants.SEX, mSex);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpConsts.UPDATE_FIELDS);
        super.onDestroy();
    }
}
