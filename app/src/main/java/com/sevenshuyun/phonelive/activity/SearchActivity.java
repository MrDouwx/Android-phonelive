package com.sevenshuyun.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.RefreshAdapter;
import com.sevenshuyun.phonelive.adapter.SearchAdapter;
import com.sevenshuyun.phonelive.bean.SearchUserBean;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.utils.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/10/25.
 */

public class SearchActivity extends AbsActivity {

    private EditText mEditText;
    private RefreshView mRefreshView;
    private SearchAdapter mSearchAdapter;
    private InputMethodManager imm;
    private String mKey;
    private MyHandler mHandler;

    public static void forward(Context context) {
        context.startActivity(new Intent(context, SearchActivity.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void main() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEditText = (EditText) findViewById(R.id.edit);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HttpUtil.cancel(HttpConsts.SEARCH);
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (!TextUtils.isEmpty(s)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                } else {
                    mKey = null;
                    if (mSearchAdapter != null) {
                        mSearchAdapter.clearData();
                    }
                    if (mRefreshView != null) {
                        mRefreshView.setRefreshEnable(false);
                        mRefreshView.setLoadMoreEnable(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_search);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<SearchUserBean>() {

            @Override
            public RefreshAdapter<SearchUserBean> getAdapter() {
                if (mSearchAdapter == null) {
                    mSearchAdapter = new SearchAdapter(mContext, Constants.FOLLOW_FROM_SEARCH);
                    mSearchAdapter.setOnItemClickListener(new OnItemClickListener<SearchUserBean>() {
                        @Override
                        public void onItemClick(SearchUserBean bean, int position) {
                            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                            UserHomeActivity.forward(mContext, bean.getId());
                        }
                    });
                }
                return mSearchAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mKey)) {
                    HttpUtil.search(mKey, p, callback);
                }
            }

            @Override
            public List<SearchUserBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), SearchUserBean.class);
            }

            @Override
            public void onRefresh(List<SearchUserBean> list) {
                if (mRefreshView != null) {
                    mRefreshView.setRefreshEnable(true);
                }
            }

            @Override
            public void onNoData(boolean noData) {
                if (mRefreshView != null) {
                    mRefreshView.setRefreshEnable(false);
                }
            }

            @Override
            public void onLoadDataCompleted(int dataCount) {
                if (mRefreshView != null) {
                    if (dataCount > 0) {
                        mRefreshView.setLoadMoreEnable(true);
                    }else{
                        mRefreshView.setLoadMoreEnable(false);
                    }
                }
            }
        });
        mHandler = new MyHandler(this);
    }

    private void search() {
        String key = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            ToastUtil.show(R.string.content_empty);
            return;
        }
        HttpUtil.cancel(HttpConsts.SEARCH);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mKey = key;
        mRefreshView.initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
//        if (imm != null && mEditText != null) {
//            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
//        }
        HttpUtil.cancel(HttpConsts.SEARCH);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.release();
        }
        mHandler = null;
        super.onDestroy();
    }

    private static class MyHandler extends Handler {

        private SearchActivity mActivity;

        public MyHandler(SearchActivity activity) {
            mActivity = new WeakReference<>(activity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity != null) {
                mActivity.search();
            }
        }

        public void release() {
            mActivity = null;
        }
    }


}
