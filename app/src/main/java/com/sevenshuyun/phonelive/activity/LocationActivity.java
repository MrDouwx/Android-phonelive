package com.sevenshuyun.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.LocationAdapter;
import com.sevenshuyun.phonelive.bean.TxLocationBean;
import com.sevenshuyun.phonelive.bean.TxLocationPoiBean;
import com.sevenshuyun.phonelive.custom.RefreshLayout;
import com.sevenshuyun.phonelive.event.LocationEvent;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.CommonCallback;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.utils.LocationUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by cxf on 2018/7/18.
 */

public class LocationActivity extends AbsActivity implements View.OnClickListener {

    private MapView mMapView;
    private TencentMap mTencentMap;
    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private View mNoData;
    private LocationAdapter mAdapter;
    private int mPage;
    private double mLng;
    private double mLat;
    private boolean mMapLoaded;
    private EditText mEditText;
    private RefreshLayout mRefreshLayout2;
    private RecyclerView mRecyclerView2;
    private LocationAdapter mAdapter2;
    private View mNoData2;
    private View mSearchResultGroup;
    private Handler mHandler;
    private static final int WHAT = 0;
    private InputMethodManager imm;
    private int mPage2;
    private String mCurKeyWord;//搜索的关键字
    private boolean mClickItem;
    private boolean mFirstGrag = true;//是否是第一次拖动

    @Override
    protected int getLayoutId() {
        return R.layout.activity_location;
    }

    @Override
    protected void main(Bundle savedInstanceState) {
        setTitle(WordUtil.getString(R.string.location));
        mRefreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                loadMorePoi();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshLayout.setScorllView(mRecyclerView);
        mNoData = findViewById(R.id.no_data);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mTencentMap = mMapView.getMap();
        mTencentMap.setZoom(16);
        mTencentMap.setOnMapLoadedListener(new TencentMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                mMapLoaded = true;
            }
        });
        mTencentMap.setOnMapCameraChangeListener(new TencentMap.OnMapCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (mFirstGrag) {
                    mFirstGrag = false;
                } else {
                    mClickItem = false;
                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (!mClickItem) {
                    LatLng latLng = cameraPosition.getTarget();
                    mLng = latLng.getLongitude();
                    mLat = latLng.getLatitude();
                    HttpUtil.cancel(HttpConsts.GET_MAP_INFO);
                    refreshPoi();
                }
                mFirstGrag = true;
                mClickItem = false;
            }
        });
        mLng = AppConfig.getInstance().getLng();
        mLat = AppConfig.getInstance().getLat();
        if (mLng == 0 || mLat == 0) {
            LocationUtil.getInstance().setNeedPostLocationEvent(true);
            LocationUtil.getInstance().startLocation();
        } else {
            showMyLocation();
        }
        EventBus.getDefault().register(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_my_location).setOnClickListener(this);
        mSearchResultGroup = findViewById(R.id.search_result_group);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEditText = (EditText) findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    HttpUtil.cancel(HttpConsts.GET_MAP_SEARCH);
                    if (mHandler != null) {
                        mHandler.removeMessages(WHAT);
                    }
                    if (mLng == 0 || mLat == 0) {
                        ToastUtil.show(WordUtil.getString(R.string.im_location_failed));
                        return true;
                    }
                    String key = mEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(key)) {
                        if (mSearchResultGroup.getVisibility() != View.VISIBLE) {
                            mSearchResultGroup.setVisibility(View.VISIBLE);
                        }
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        //搜索地点
                        mPage2 = 1;
                        mCurKeyWord = key;
                        HttpUtil.searchAddressInfoByTxLocaitonSdk(mLng, mLat, key, mPage2, mRefreshCallback2);
                    } else {
                        ToastUtil.show(WordUtil.getString(R.string.content_empty));
                    }
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
                HttpUtil.cancel(HttpConsts.GET_MAP_SEARCH);
                if (!TextUtils.isEmpty(s)) {
                    if (mHandler != null) {
                        mHandler.removeMessages(WHAT);
                        mHandler.sendEmptyMessageDelayed(WHAT, 500);
                    }
                } else {
                    if (mSearchResultGroup.getVisibility() == View.VISIBLE) {
                        mSearchResultGroup.setVisibility(View.INVISIBLE);
                    }
                    if (mAdapter2 != null) {
                        mAdapter2.clear();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                HttpUtil.cancel(HttpConsts.GET_MAP_SEARCH);
                String key = mEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(key)) {
                    if (mSearchResultGroup.getVisibility() != View.VISIBLE) {
                        mSearchResultGroup.setVisibility(View.VISIBLE);
                    }
                    //搜索地点
                    mPage2 = 1;
                    mCurKeyWord = key;
                    HttpUtil.searchAddressInfoByTxLocaitonSdk(mLng, mLat, key, mPage2, mRefreshCallback2);
                } else {
                    if (mSearchResultGroup.getVisibility() == View.VISIBLE) {
                        mSearchResultGroup.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        mRefreshLayout2 = (RefreshLayout) findViewById(R.id.refreshLayout2);
        mRefreshLayout2.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                mPage2++;
                HttpUtil.searchAddressInfoByTxLocaitonSdk(mLng, mLat, mCurKeyWord, mPage2, mLoadMoreCallback2);
            }
        });
        mRecyclerView2 = (RecyclerView) findViewById(R.id.recyclerView2);
        mRecyclerView2.setHasFixedSize(true);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshLayout2.setScorllView(mRecyclerView2);
        mNoData2 = findViewById(R.id.no_data2);

    }


    /**
     * 在地图上显示自己的位置
     */
    private void showMyLocation() {
        mTencentMap.setCenter(new LatLng(mLat, mLng));
    }


    private void refreshPoi() {
        mPage = 1;
        HttpUtil.getAddressInfoByTxLocaitonSdk(mLng, mLat, 1, mPage, HttpConsts.GET_MAP_INFO, mRefreshCallback);
    }

    private void loadMorePoi() {
        mPage++;
        HttpUtil.getAddressInfoByTxLocaitonSdk(mLng, mLat, 1, mPage, HttpConsts.GET_MAP_INFO, mLoadMoreCallback);
    }

    private CommonCallback<TxLocationBean> mRefreshCallback = new CommonCallback<TxLocationBean>() {
        @Override
        public void callback(TxLocationBean bean) {
            List<TxLocationPoiBean> list = bean.getPoiList();
            if (list != null && list.size() > 0) {
                if (mNoData != null && mNoData.getVisibility() == View.VISIBLE) {
                    mNoData.setVisibility(View.INVISIBLE);
                }
                if (mRecyclerView != null) {
                    if (mAdapter == null) {
                        mAdapter = new LocationAdapter(mContext, list);
                        mAdapter.setOnItemClickListener(new OnItemClickListener<TxLocationPoiBean>() {
                            @Override
                            public void onItemClick(TxLocationPoiBean bean, int position) {
                                mClickItem = true;
                                TxLocationPoiBean.Location location = bean.getLocation();
                                mTencentMap.setCenter(new LatLng(location.getLat(), location.getLng()));
                            }
                        });
                        mRecyclerView.setAdapter(mAdapter);
                    } else {
                        mAdapter.setList(list);
                    }
                    mRecyclerView.scrollToPosition(0);
                }
                if (mRefreshLayout != null) {
                    if (list.size() < 20) {
                        mRefreshLayout.setLoadMoreEnable(false);
                    } else {
                        mRefreshLayout.setLoadMoreEnable(true);
                    }
                }
            } else {
                if (mAdapter != null) {
                    mAdapter.clear();
                }
                if (mNoData != null && mNoData.getVisibility() != View.VISIBLE) {
                    mNoData.setVisibility(View.VISIBLE);
                }
                if (mRefreshLayout != null) {
                    mRefreshLayout.setLoadMoreEnable(false);
                }
            }

        }
    };

    private CommonCallback<TxLocationBean> mLoadMoreCallback = new CommonCallback<TxLocationBean>() {
        @Override
        public void callback(TxLocationBean bean) {
            if (mRefreshLayout != null) {
                mRefreshLayout.completeLoadMore();
            }
            List<TxLocationPoiBean> list = bean.getPoiList();
            if (list != null && list.size() > 0) {
                if (mAdapter != null) {
                    mAdapter.insertList(list);
                }
                if (list.size() < 20 && mRefreshLayout != null) {
                    mRefreshLayout.setLoadMoreEnable(false);
                }
            } else {
                ToastUtil.show(WordUtil.getString(R.string.no_more_data));
                mPage--;
                if (mRefreshLayout != null) {
                    mRefreshLayout.setLoadMoreEnable(false);
                }
            }

        }
    };

    private CommonCallback<List<TxLocationPoiBean>> mRefreshCallback2 = new CommonCallback<List<TxLocationPoiBean>>() {
        @Override
        public void callback(List<TxLocationPoiBean> list) {
            if (list != null && list.size() > 0) {
                if (mNoData2 != null && mNoData2.getVisibility() == View.VISIBLE) {
                    mNoData2.setVisibility(View.INVISIBLE);
                }
                if (mRecyclerView2 != null) {
                    if (mAdapter2 == null) {
                        mAdapter2 = new LocationAdapter(mContext, list);
                        mAdapter2.setOnItemClickListener(new OnItemClickListener<TxLocationPoiBean>() {
                            @Override
                            public void onItemClick(TxLocationPoiBean bean, int position) {
                                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                                if (mSearchResultGroup != null && mSearchResultGroup.getVisibility() == View.VISIBLE) {
                                    mSearchResultGroup.setVisibility(View.INVISIBLE);
                                }
                                if (mAdapter2 != null) {
                                    mAdapter2.clear();
                                }
                                if (mEditText != null) {
                                    mEditText.setText("");
                                }
                                TxLocationPoiBean.Location location = bean.getLocation();
                                mTencentMap.setCenter(new LatLng(location.getLat(), location.getLng()));
                            }
                        });
                        mRecyclerView2.setAdapter(mAdapter2);
                    } else {
                        mAdapter2.setList(list);
                    }
                }
                if (mRefreshLayout2 != null) {
                    if (list.size() < 20) {
                        mRefreshLayout2.setLoadMoreEnable(false);
                    } else {
                        mRefreshLayout2.setLoadMoreEnable(true);
                    }
                }
            } else {
                if (mAdapter2 != null) {
                    mAdapter2.clear();
                }
                if (mNoData2 != null && mNoData2.getVisibility() != View.VISIBLE) {
                    mNoData2.setVisibility(View.VISIBLE);
                }
                if (mRefreshLayout2 != null) {
                    mRefreshLayout2.setLoadMoreEnable(false);
                }
            }

        }
    };

    private CommonCallback<List<TxLocationPoiBean>> mLoadMoreCallback2 = new CommonCallback<List<TxLocationPoiBean>>() {
        @Override
        public void callback(List<TxLocationPoiBean> list) {
            if (mRefreshLayout2 != null) {
                mRefreshLayout2.completeLoadMore();
            }
            if (list != null && list.size() > 0) {
                if (mAdapter2 != null) {
                    mAdapter2.insertList(list);
                }
                if (list.size() < 20 && mRefreshLayout2 != null) {
                    mRefreshLayout2.setLoadMoreEnable(false);
                }
            } else {
                ToastUtil.show(WordUtil.getString(R.string.no_more_data));
                mPage--;
                if (mRefreshLayout2 != null) {
                    mRefreshLayout2.setLoadMoreEnable(false);
                }
            }

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationEvent e) {
        mLng = e.getLng();
        mLat = e.getLat();
        showMyLocation();
    }

    @Override
    public void onBackPressed() {
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpConsts.GET_MAP_SEARCH);
        HttpUtil.cancel(HttpConsts.GET_MAP_INFO);
        LocationUtil.getInstance().setNeedPostLocationEvent(false);
        EventBus.getDefault().unregister(this);
        if (mMapView != null) {
            mMapView.stopAnimation();
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (mMapView != null) {
            mMapView.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        if (mMapView != null) {
            mMapView.onRestart();
        }
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    private void sendLocationInfo() {
        if (!mMapLoaded) {
            ToastUtil.show(WordUtil.getString(R.string.im_map_not_loaded));
            return;
        }
        if (mAdapter != null) {
            TxLocationPoiBean bean = null;
            if (mSearchResultGroup.getVisibility() == View.VISIBLE) {
                bean = mAdapter2.getCheckedLocationPoiBean();
            } else {
                bean = mAdapter.getCheckedLocationPoiBean();
            }
            if (bean != null) {
                Intent intent = new Intent();
                TxLocationPoiBean.Location location = bean.getLocation();
                intent.putExtra(Constants.LAT, location.getLat());
                intent.putExtra(Constants.LNG, location.getLng());
                intent.putExtra(Constants.SCALE, mTencentMap.getZoomLevel());
                String address = "{\"name\":\"" + bean.getTitle() + "\",\"info\":\"" + bean.getAddress() + "\"}";
                intent.putExtra(Constants.ADDRESS, address);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                ToastUtil.show(R.string.im_address_failed);
            }
        } else {
            ToastUtil.show(R.string.im_address_failed);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                sendLocationInfo();
                break;
            case R.id.btn_my_location:
                moveToMyLocation();
                break;
        }
    }

    private void moveToMyLocation() {
        mLng = AppConfig.getInstance().getLng();
        mLat = AppConfig.getInstance().getLat();
        showMyLocation();
    }
}
