package hxyd.nimdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hxyd.mynimdemo.NimDemo;
import hxyd.nimdemo.Util.AES;
import hxyd.nimdemo.Util.EncryptionByMD5;
import hxyd.nimdemo.Util.RSASignature;

/**
 * Created by admin on 2019/6/3.
 */

public class BaseApp extends MultiDexApplication {
    private static BaseApp _instance;
    public AES aes;
    public SharedPreferences spn_gjj;
    public SharedPreferences.Editor editor_gjj;
    public static String ANDROID_ID;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;

        aes = new AES();
        ANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        spn_gjj = getSharedPreferences("gjj", MODE_PRIVATE);
        editor_gjj = spn_gjj.edit();

        setUserId("");
        setDeviceType("2");
        setDeviceToken(ANDROID_ID);
        setCurrenVersion(getAppVersionName(BaseApp.this));
        setUserIdType("");


        NimDemo.getNimDemo().init(getApplicationContext(),"http://zt.wx.pangjiachen.com/miapp/TEST.A0117/gateway");
    }


    public static BaseApp getInstance() {
        return _instance;
    }
    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
    public String getUserId() {
        return spn_gjj.getString("TAISxiqUdRqoR71JLHjztw==", "");
    }
    public void setUserId(String userId) {
        editor_gjj.putString("TAISxiqUdRqoR71JLHjztw==", userId);
        editor_gjj.commit();
    }
    public String getDeviceType() {
        return spn_gjj.getString("K0FlPj4FDcW/aYyHdA8uJQ==", "");
    }

    public void setDeviceType(String deviceType) {
        editor_gjj.putString("K0FlPj4FDcW/aYyHdA8uJQ==", deviceType);
        editor_gjj.commit();
    }
    public String getDeviceToken() {
        return spn_gjj.getString("mPjwVstkBCapUvny19fhgA==", "");
    }

    public void setDeviceToken(String deviceToken) {
        editor_gjj.putString("mPjwVstkBCapUvny19fhgA==", deviceToken);
        editor_gjj.commit();
    }
    public String getCurrenVersion() {
        return spn_gjj.getString("POc3dfbT4NaL5BJvNHtK0A==", "");
    }

    public void setCurrenVersion(String currenVersion) {
        editor_gjj.putString("POc3dfbT4NaL5BJvNHtK0A==", currenVersion);
        editor_gjj.commit();
    }
    public String getUserIdType() {
        return spn_gjj.getString("userIdType==", "");
    }

    public void setUserIdType(String userIdType) {
        editor_gjj.putString("userIdType==", userIdType);
        editor_gjj.commit();
    }

    private String timesign;

    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return sdf.format(date);
    }
    public Map<String, Object> getZdyRequestParams(String buztype) {
        Map<String, Object> params = new HashMap<String, Object>();
        timesign = BaseApp.getInstance().aes.encrypt(getCurrentTime());
        String rsa = RSASignature.sign(EncryptionByMD5.getMD5(getSignValue(buztype).getBytes()), RSASignature.RSA_PRIVATE);
        params.put("state", "app");//固定
        params.put("__openid", BaseApp.getInstance().getUserId());//uesrId
        params.put("__timestamp", timesign);//时间加密
        params.put("deviceType", BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getDeviceType()));
        params.put("deviceToken", BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getDeviceToken()));
        params.put("currenVersion", BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getCurrenVersion()));
        params.put("userIdType",BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getUserIdType()));
        params.put("buztype", BaseApp.getInstance().aes.encrypt(buztype));
        params.put("__para", BaseApp.getInstance().aes.encrypt(getParaValue()));
        params.put("__sign", rsa);
        return params;
    }

    public String getParaValue() {
        return "__timestamp,deviceType,deviceToken,currenVersion,userIdType,buztype";
    }
    public String getSignValue(String buztype) {
        return "__timestamp=" + timesign
                + "&deviceType=" + BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getDeviceType())
                + "&deviceToken=" + BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getDeviceToken())
                + "&currenVersion=" + BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getCurrenVersion())
                + "&userIdType=" + BaseApp.getInstance().aes.encrypt(BaseApp.getInstance().getUserIdType())
                + "&buztype=" + BaseApp.getInstance().aes.encrypt(buztype);
    }
}
