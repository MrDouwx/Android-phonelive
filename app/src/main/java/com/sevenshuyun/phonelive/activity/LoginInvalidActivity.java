package com.sevenshuyun.phonelive.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.AppContext;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.event.LoginInvalidEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2017/10/9.
 * 登录失效的时候以dialog形式弹出的activity
 */

public class LoginInvalidActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(String tip) {
        Intent intent = new Intent(AppContext.sInstance, LoginInvalidActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.TIP, tip);
        AppContext.sInstance.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_invalid;
    }

    @Override
    protected void main() {
        TextView textView = (TextView) findViewById(R.id.content);
        String tip = getIntent().getStringExtra(Constants.TIP);
        textView.setText(tip);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new LoginInvalidEvent());
        AppConfig.getInstance().clearLoginInfo();
        //友盟统计登出
        MobclickAgent.onProfileSignOff();
        LoginActivity.forward();
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
