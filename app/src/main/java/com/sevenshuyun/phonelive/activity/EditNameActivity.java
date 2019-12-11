package com.sevenshuyun.phonelive.activity;

import android.content.Intent;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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
 * 设置昵称
 */

public class EditNameActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_name;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.edit_profile_update_nickname));
        mEditText = (EditText) findViewById(R.id.edit);
        mEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(8)
        });
        findViewById(R.id.btn_save).setOnClickListener(this);
        String content = getIntent().getStringExtra(Constants.NICK_NAME);
        if (!TextUtils.isEmpty(content)) {
            if (content.length() > 8) {
                content = content.substring(0, 8);
            }
            mEditText.setText(content);
            mEditText.setSelection(content.length());
        }
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        final String content = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(R.string.edit_profile_name_empty);
            return;
        }
        HttpUtil.updateFields("{\"user_nicename\":\"" + content + "\"}", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        ToastUtil.show(obj.getString("msg"));
                        UserBean u = AppConfig.getInstance().getUserBean();
                        if (u != null) {
                            u.setUserNiceName(content);
                        }
                        Intent intent = getIntent();
                        intent.putExtra(Constants.NICK_NAME, content);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    ToastUtil.show(msg);
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
