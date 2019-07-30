package hxyd.mynimdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderTip;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.util.NIMUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2019/5/14.
 */

public class NimDemo {
    private static NimDemo nimDemo;
    private Context mContext;

    public static NimDemo getNimDemo() {
        if (null == nimDemo) {
            synchronized (NimDemo.class) {
                if (null == nimDemo) {
                    nimDemo = new NimDemo();
                }
            }
        }
        return nimDemo;
    }

    /**
     *
     * @param context
     * @param url 咨询接口
     */
    public void init(final Context context,String url) {
//        SDKOptions options = new SDKOptions();
//        options.appKey=appKey;
        NIMClient.init(context, null, null);
        mUrl = url;
        if (NIMUtil.isMainProcess(context)){
            NimUIKit.init(context);
            // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
            NimUIKit.setCustomPushContentProvider(new DemoPushContentProvider());
            NimUIKit.setLocationProvider(new NimDemoLocationProvider());
            NimUIKit.registerTipMsgViewHolder(MsgViewHolderTip.class);
        }
    }

    private JSONObject getLoginParams(Map<String, Object> params, String action) {

        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            StringBuilder data = new StringBuilder();
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    data.append(entry.getKey()).append("=");
                    data.append(URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8"));// 编码
                    data.append('&');
                }
                data.deleteCharAt(data.length() - 1);
            }

            byte[] entity = data.toString().getBytes(); // 得到实体数据
            HttpURLConnection connection = (HttpURLConnection) new URL(action).openConnection();
            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(120000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(entity.length));
            connection.setDoOutput(true);
            outputStream = connection.getOutputStream();
            outputStream.write(entity);
            outputStream.flush();
            StringBuffer sb = new StringBuffer();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String readLine;
                BufferedReader responseReader;
                inputStream = connection.getInputStream();
                //处理响应流，必须与服务器响应流输出的编码一致
                responseReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                return new JSONObject(sb.toString());
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject loginJsonobject = null;
    private String imAccId;
    private String imToken;
    private String userId;
    private String mUrl;
    private String nmcUrl;
    private String nmcToken;
    private String nickName;
    private Map<String,Object> mParams;

    public void getTokenLogin(Context context, final Map<String, Object> params, final String action) {
        mContext = context;
        mParams = params;
        new Thread(new Runnable() {
            @Override
            public void run() {
                loginJsonobject = getLoginParams(params, action);
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            }
        }).start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        String recode = "";
                        String msg1 = "";
                        if (loginJsonobject != null) {
                            Log.i("ABCDE", "===" + loginJsonobject.toString());
                            if (loginJsonobject.has("recode")) {
                                recode = loginJsonobject.getString("recode");
                            }if (loginJsonobject.has("msg")) {
                                msg1 = loginJsonobject.getString("msg");
                            }
                            if("000000".equals(recode)){
                                if (loginJsonobject.has("url")){
                                    nmcUrl = loginJsonobject.getString("url");
                                }
                                if (loginJsonobject.has("token")){
                                    nmcToken = loginJsonobject.getString("token");
                                }
                                if (loginJsonobject.has("data")){
                                    JSONObject data = loginJsonobject.getJSONObject("data");
                                    if (data.has("imAccId")){
                                        imAccId = data.getString("imAccId");
                                    }
                                    if (data.has("imToken")){
                                        imToken = data.getString("imToken");
                                    }
                                    if (data.has("nickName")){
                                        nickName = data.getString("nickName");
                                    }
                                    if (data.has("userId")){
                                        userId = data.getString("userId");
                                    }
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i("ABCDE","imAccId=="+imAccId+"==imToken=="+imToken);

                                            NIMClient.getService(AuthService.class).login(new LoginInfo(imAccId, imToken))
                                                    .setCallback(new RequestCallback() {
                                                        @Override
                                                        public void onSuccess(Object o) {
                                                            Log.i("ABCDE","qqq=="+o.toString());
                                                            NimUIKit.setAccount(imAccId);
                                                            mParams.put("userid",userId);
                                                            NimUIKit.startP2PSession(nickName,nmcUrl,nmcToken,mUrl,mParams,mContext,"12345678");
                                                        }
                                                        @Override
                                                        public void onFailed(int i) {
                                                            Log.i("ABCDE","qqq=="+i);
                                                        }
                                                        @Override
                                                        public void onException(Throwable throwable) {
                                                            Log.i("ABCDE","qqq=="+throwable.toString());
                                                        }
                                                    });
                                        }
                                    }).start();
                                }
                            }else{
                                ToastHelper.showToast(mContext, msg1);
                            }
                        } else {
                            ToastHelper.showToast(mContext, "返回数据为空");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

}
