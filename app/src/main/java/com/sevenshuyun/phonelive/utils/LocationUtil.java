package com.sevenshuyun.phonelive.utils;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.AppContext;
import com.sevenshuyun.phonelive.bean.TxLocationBean;
import com.sevenshuyun.phonelive.event.LocationEvent;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.CommonCallback;

import org.greenrobot.eventbus.EventBus;

public class LocationUtil {
    private static final String TAG = "定位";
    private static LocationUtil sInstance;
    private TencentLocationManager mLocationManager;
    private boolean mLocationStarted;
    private boolean mNeedPostLocationEvent;//是否需要发送定位成功事件

    private LocationUtil() {
        mLocationManager = TencentLocationManager.getInstance(AppContext.sInstance);
    }

    public static LocationUtil getInstance() {
        if (sInstance == null) {
            synchronized (LocationUtil.class) {
                if (sInstance == null) {
                    sInstance = new LocationUtil();
                }
            }
        }
        return sInstance;
    }


    private TencentLocationListener mLocationListener = new TencentLocationListener() {
        @Override
        public void onLocationChanged(TencentLocation location, int code, String reason) {
            if (code == TencentLocation.ERROR_OK) {
                double lng = location.getLongitude();//经度
                double lat = location.getLatitude();//纬度
                L.e(TAG, "获取经纬度成功------>经度：" + lng + "，纬度：" + lat);
                HttpUtil.getAddressInfoByTxLocaitonSdk(lng, lat, 0, 1, HttpConsts.GET_LOCAITON, mCallback);
                if (mNeedPostLocationEvent) {
                    EventBus.getDefault().post(new LocationEvent(lng, lat));
                }
            }
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {

        }
    };

    private CommonCallback<TxLocationBean> mCallback = new CommonCallback<TxLocationBean>() {
        @Override
        public void callback(TxLocationBean bean) {
            L.e(TAG, "获取位置信息成功---当前地址--->" + bean.getAddress());
            AppConfig.getInstance().setLocationInfo(
                    bean.getLng(), bean.getLat(), bean.getProvince(), bean.getCity(), bean.getDistrict());
        }
    };


    //启动定位
    public void startLocation() {
        if (!mLocationStarted && mLocationManager != null) {
            mLocationStarted = true;
            L.e(TAG, "开启定位");
            TencentLocationRequest request = TencentLocationRequest
                    .create()
                    .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO)
                    .setInterval(60 * 60 * 1000);//1小时定一次位

            //当定位周期大于0时, 不论是否有得到新的定位结果, 位置监听器都会按定位周期定时被回调;
            // 当定位周期等于0时, 仅当有新的定位结果时, 位置监听器才会被回调(即, 回调时机存在不确定性).
            // 如果需要周期性回调, 建议将 定位周期 设置为 5000-10000ms
            mLocationManager.requestLocationUpdates(request, mLocationListener);
        }
    }

    //停止定位
    public void stopLocation() {
        HttpUtil.cancel(HttpConsts.GET_LOCAITON);
        if (mLocationStarted && mLocationManager != null) {
            L.e(TAG, "关闭定位");
            mLocationManager.removeUpdates(mLocationListener);
            mLocationStarted = false;
        }
    }

    public void setNeedPostLocationEvent(boolean needPostLocationEvent) {
        mNeedPostLocationEvent = needPostLocationEvent;
    }

}
