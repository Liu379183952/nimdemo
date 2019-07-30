package com.netease.nim.uikit.business.session.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.uikit.MyDialog;
import com.netease.nim.uikit.MyDialog_AvChat;
import com.netease.nim.uikit.MyDialog_zxzx;
import com.netease.nim.uikit.MyToolBar;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.ScoreBean;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.main.CustomPushContentProvider;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.avchat.AVChatAction;
import com.netease.nim.uikit.business.ait.AitManager;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.actions.ImageAction;
import com.netease.nim.uikit.business.session.actions.LocationAction;
import com.netease.nim.uikit.business.session.actions.VideoAction;
import com.netease.nim.uikit.business.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.business.session.module.input.InputPanel;
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.InvocationFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;
import com.netease.nimlib.sdk.msg.model.NIMAntiSpamOption;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.robot.model.RobotMsgType;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 聊天界面基类
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class MessageFragment extends TFragment implements ModuleProxy {

    private View rootView;

    private SessionCustomization customization;

    protected static final String TAG = "MessageActivity";

    // p2p对方Account或者群id
    protected String sessionId;

    protected SessionTypeEnum sessionType;

    // modules
    protected InputPanel inputPanel;
    protected MessageListPanelEx messageListPanel;

    protected AitManager aitManager;
    protected Map<String, Object> map;
    protected Map<String, Object> mapLogin;
    private String chatId = "";
    String imAccId = "", imToken = "", userId = "";
    private String mUrl;
    private String nickName;
    PostFormBuilder pfb;
    private boolean isVisibility = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseIntent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.nim_message_fragment, container, false);
        return rootView;
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    public void onPause() {
        super.onPause();
        isVisibility = false;

        NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        inputPanel.onPause();
        messageListPanel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisibility = true;
        messageListPanel.onResume();
        NIMClient.getService(MsgService.class).setChattingAccount(sessionId, sessionType);
        getActivity().setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);//媒体音量扬声器播放
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isVisibility = false;
        messageListPanel.onDestroy();
        registerObservers(false);
        if (inputPanel != null) {
            inputPanel.onDestroy();
        }
        if (aitManager != null) {
            aitManager.reset();
        }
    }

    public boolean onBackPressed() {
        return inputPanel.collapse(true) || messageListPanel.onBackPressed();
    }

    public void refreshMessageList() {
        messageListPanel.refreshMessageList();
    }

    private void parseIntent() {
        Bundle arguments = getArguments();
        map = (Map<String, Object>) JSONObject.parseObject(arguments.getString(Extras.PARAMS));
        mapLogin = (Map<String, Object>) JSONObject.parseObject(arguments.getString(Extras.PARAMS));
        mUrl = arguments.getString(Extras.URL);
        nickName = arguments.getString(Extras.nickName);
        sessionId = arguments.getString(Extras.EXTRA_ACCOUNT);
        sessionType = (SessionTypeEnum) arguments.getSerializable(Extras.EXTRA_TYPE);
        IMMessage anchor = (IMMessage) arguments.getSerializable(Extras.EXTRA_ANCHOR);

        customization = (SessionCustomization) arguments.getSerializable(Extras.EXTRA_CUSTOMIZATION);
        Container container = new Container(getActivity(), sessionId, sessionType, this, true);

        if (messageListPanel == null) {
            messageListPanel = new MessageListPanelEx(nickName,container, rootView, anchor, false, false);
        } else {
            messageListPanel.reload(container, anchor);
        }

        if (inputPanel == null) {
            inputPanel = new InputPanel(container, rootView, getActionList());
            inputPanel.setCustomization(customization);
        } else {
            inputPanel.reload(container, customization);
        }

        initAitManager();

        inputPanel.switchRobotMode(NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(sessionId) != null);

        registerObservers(true);

        if (customization != null) {
            messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
        }
        isVisibility = true;
        MyToolBar toolBar = (MyToolBar) rootView.findViewById(R.id.toolbar);
        toolBar.setTitle("在线咨询");
        toolBar.setLeftButtonOnClickLinster(new View.OnClickListener() {
            public void onClick(View view) {
                if ("".equals(chatId) || chatId == null) {
                    NIMClient.getService(AuthService.class).logout();
                    NimUIKit.setAccount("");
                    getActivity().finish();
                } else {
                    showDoLgoutDialog();
                }
            }
        });
    }

    private void initAitManager() {
        UIKitOptions options = NimUIKitImpl.getOptions();
        if (options.aitEnable) {
            aitManager = new AitManager(getContext(), options.aitTeamMember && sessionType == SessionTypeEnum.Team ? sessionId : null, options.aitIMRobot);
            inputPanel.addAitTextWatcher(aitManager);
            aitManager.setTextChangeListener(inputPanel);
        }
    }

    /**
     * ************************* 消息收发 **********************************
     */
    // 是否允许发送消息
    protected boolean isAllowSendMessage(final IMMessage message) {
        return customization.isAllowSendMessage(message);
    }


    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(commandObserver, register);
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeReceiveMessage(incomingMessageObserver, register);
        // 已读回执监听
        if (NimUIKitImpl.getOptions().shouldHandleReceipt) {
            service.observeMessageReceipt(messageReceiptObserver, register);
        }
    }

    /**
     * 命令消息接收观察者
     */
    private Observer<CustomNotification> commandObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification message) {
            showCommandMessage(message);
        }
    };

    protected void showCommandMessage(CustomNotification message) {
        String content = message.getContent();
        Log.i("ABCDE","content=="+content);
        JSONObject json = JSON.parseObject(content);
        int id = json.getIntValue("id");
        if (id == 1) {
        } else {
            String msg = "";
            String type = "";
            String KFimAccId = "";
            String chatType = "";
            String nickName = "";
            int avChatType = 0;
            if (json.containsKey("type")) {
                type = json.getString("type");
            }
            if (json.containsKey("body")) {
                JSONObject body = json.getJSONObject("body");
                if (body.containsKey("msg")) {
                    msg = body.getString("msg");
                }
                if (body.containsKey("imAccId")) {
                    KFimAccId = body.getString("imAccId");
                }
                if (body.containsKey("type")) {
                    chatType = body.getString("type");
                }
                if (body.containsKey("nickName")) {
                    nickName = body.getString("nickName");
                }

                if (!"1".equals(type)) {
                    if ("5".equals(type)) {
                        if (chatId == null || "".equals(chatId)) {
                        } else {
                            if (dialog_avChat.isShowing()) {
                                dialog_avChat.dismiss();
                            }
                            if ("0".equals(chatType)) {
                                avChatType = 1;
                            } else if ("1".equals(chatType)) {
                                avChatType = 2;
                            }
                            map.put("wkfFlag", "12");
                            map.put("type", "");
                            map.put("chat_id", chatId);
                            AVChatActivity.outgoingCall(getActivity(), KFimAccId, nickName, avChatType, AVChatActivity.FROM_INTERNAL,map,mUrl);
//                            AVChatKit.outgoingCall(getActivity(), KFimAccId, nickName, avChatType, AVChatActivity.FROM_INTERNAL);
                        }
                    } else {
                        if ("2".equals(type) || "12".equals(type)) {
                            NimUIKit.setAccount("");
                            NIMClient.getService(AuthService.class).logout();
                            final IMMessage tipMessage = MessageBuilder.createTipMessage("12345678", SessionTypeEnum.P2P);
                            tipMessage.setContent(msg);
                            CustomMessageConfig config = new CustomMessageConfig();
                            tipMessage.setConfig(config);
                            tipMessage.setStatus(MsgStatusEnum.success);
                            sendMessage(tipMessage, null, "");
                            chatId = "";
                        }else{
                            Log.i("123456","type=="+type);
                            final IMMessage tipMessage = MessageBuilder.createTipMessage("12345678", SessionTypeEnum.P2P);
                            tipMessage.setContent(msg);
                            CustomMessageConfig config = new CustomMessageConfig();
                            tipMessage.setConfig(config);
                            tipMessage.setStatus(MsgStatusEnum.success);
                            sendMessage(tipMessage, null, "");
                        }
                    }
                } else {
                    if (body.containsKey("scoreTemplate")) {
                        mList = new ArrayList<ScoreBean>();
                        JSONArray scoreTemplate = body.getJSONArray("scoreTemplate");
                        Gson gson = new Gson();
                        mList = gson.fromJson(scoreTemplate.toString(), new TypeToken<LinkedList<ScoreBean>>() {
                        }.getType());
                        if (isVisibility) {
                            if (chatId != null || !"".equals(chatId)) {
                                showScoreDialog("1");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            Log.i("123456789","==="+NimUIKit.getAccount());
            Log.i("123456789","=1=="+messages.get(0).getFromAccount());
            if (messages.get(0).getFromAccount().equals(NimUIKit.getAccount())) {
                messages.remove(0);
                return;
            }else{
                List<IMMessage>  item = new ArrayList<>();
                if (NimUIKitImpl.history.containsKey(nickName)){
                    item.addAll(NimUIKitImpl.history.get(nickName));
                    item.add(messages.get(0));
                }else{
                    item.add(messages.get(0));
                }
                NimUIKitImpl.history.put(nickName,item);
                onMessageIncoming(messages);
            }
        }
    };



    private void onMessageIncoming(List<IMMessage> messages) {
        if (CommonUtil.isEmpty(messages)) {
            return;
        }
        messageListPanel.onIncomingMessage(messages);
        // 发送已读回执
        messageListPanel.sendReceipt();
    }

    /**
     * 已读回执观察者
     */
    private Observer<List<MessageReceipt>> messageReceiptObserver = new Observer<List<MessageReceipt>>() {
        @Override
        public void onEvent(List<MessageReceipt> messageReceipts) {
            messageListPanel.receiveReceipt();
        }
    };


    /**
     * ********************** implements ModuleProxy *********************
     */
    @Override
    public boolean sendMessage(IMMessage message, File file, String location) {
        if ("".equals(NimUIKit.getAccount())) {
            mapLogin.put("userid", "");
            resetLogin(mapLogin, message, file, location);
        } else {
            if (message == null) {
                org.json.JSONObject object = new org.json.JSONObject();
                try {
                    object.put("type", location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("wkfFlag", "4");
                map.put("type", "100");
                map.put("datas", object.toString());
                sendMessageTemp(map, message);
            } else {
                if (message.getMsgType() == MsgTypeEnum.text) {
                    map.put("wkfFlag", "4");
                    map.put("type", "0");
                    map.put("datas", message.getContent());
                    sendMessageTemp(map, message);
                } else if (message.getMsgType() == MsgTypeEnum.image) {
                    map.put("wkfFlag", "8");
                    map.put("type", "1");
                    sendPicMessageTemp(map, message, file);
                } else if (message.getMsgType() == MsgTypeEnum.audio) {
                    map.put("wkfFlag", "8");
                    map.put("type", "2");
                    sendPicMessageTemp(map, message, file);
                } else if (message.getMsgType() == MsgTypeEnum.video) {
                    map.put("wkfFlag", "8");
                    map.put("type", "3");
                    sendPicMessageTemp(map, message, file);
                } else if (message.getMsgType() == MsgTypeEnum.location) {
                    map.put("wkfFlag", "4");
                    map.put("type", "4");
                    map.put("datas", location);
                    sendMessageTemp(map, message);
                } else if (message.getMsgType() == MsgTypeEnum.tip) {
                    mySendMessage(message);
                }
            }
        }
        return true;
    }

    private void mySendMessage(IMMessage message) {

        List<IMMessage>  item = new ArrayList<>();
        if (NimUIKitImpl.history.containsKey(nickName)){
            item.addAll(NimUIKitImpl.history.get(nickName));
            item.add(message);
        }else{
            item.add(message);
        }
        NimUIKitImpl.history.put(nickName,item);
        appendTeamMemberPush(message);
        message = changeToRobotMsg(message);
        final IMMessage msg = message;
        appendPushConfig(message);
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {

            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
        messageListPanel.onMsgSend(message);
        if (aitManager != null) {
            aitManager.reset();
        }
    }

    /**
     * 云信重新登录
     *
     * @param mapTemp
     * @param message
     * @param file
     * @param location
     */
    private void resetLogin(final Map<String, Object> mapTemp, final IMMessage message, final File file, final String location) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                String recode = "";
                String msg = "";
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    if ("000000".equals(recode)) {
                        if (obj.has("data")) {
                            org.json.JSONObject data = obj.getJSONObject("data");
                            if (data.has("imAccId")) {
                                imAccId = data.getString("imAccId");
                            }
                            if (data.has("imToken")) {
                                imToken = data.getString("imToken");
                            }
                            if (data.has("userId")) {
                                userId = data.getString("userId");
                            }
                            NIMClient.getService(AuthService.class).login(new LoginInfo(imAccId, imToken))
                                    .setCallback(new RequestCallback() {
                                        @Override
                                        public void onSuccess(Object o) {
                                            NimUIKit.setAccount(imAccId);
                                            map.put("userid", userId);
                                            if (message == null) {
                                                org.json.JSONObject object = new org.json.JSONObject();
                                                try {
                                                    object.put("type", location);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                map.put("wkfFlag", "4");
                                                map.put("type", "100");
                                                map.put("datas", object.toString());
                                                sendMessageTemp(map, message);
                                            } else {
                                                if (message.getMsgType() == MsgTypeEnum.text) {
                                                    map.put("wkfFlag", "4");
                                                    map.put("type", "0");
                                                    map.put("datas", message.getContent());
                                                    sendMessageTemp(map, message);
                                                } else if (message.getMsgType() == MsgTypeEnum.image) {
                                                    map.put("wkfFlag", "8");
                                                    map.put("type", "1");
                                                    sendPicMessageTemp(map, message, file);
                                                } else if (message.getMsgType() == MsgTypeEnum.audio) {
                                                    map.put("wkfFlag", "8");
                                                    map.put("type", "2");
                                                    sendPicMessageTemp(map, message, file);
                                                } else if (message.getMsgType() == MsgTypeEnum.video) {
                                                    map.put("wkfFlag", "8");
                                                    map.put("type", "3");
                                                    sendPicMessageTemp(map, message, file);
                                                } else if (message.getMsgType() == MsgTypeEnum.location) {
                                                    map.put("wkfFlag", "4");
                                                    map.put("type", "4");
                                                    map.put("datas", location);
                                                    sendMessageTemp(map, message);
                                                } else if (message.getMsgType() == MsgTypeEnum.tip) {
                                                    mySendMessage(message);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailed(int i) {
                                            Log.i("ABCDE", "qqq==" + i);
                                        }

                                        @Override
                                        public void onException(Throwable throwable) {
                                            Log.i("ABCDE", "qqq==" + throwable.toString());
                                        }
                                    });
                        }

                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发送文本消息，位置消息
     *
     * @param mapTemp
     * @param message
     */
    private void sendMessageTemp(final Map<String, Object> mapTemp, final IMMessage message) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                String recode = "";
                String msg = "";
                Log.i("123456", "response==" + response);
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    if (obj.has("data")) {
                        org.json.JSONObject data = obj.getJSONObject("data");
                        if (data.has("chatId")) {
                            chatId = data.getString("chatId");
                        }
                    }
                    if ("000000".equals(recode)) {
                        if (message == null) {
                            showWaitPage();
                        } else {
                            mySendMessage(message);
                        }
                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发送图片、视频、语音等消息
     *
     * @param mapTemp 公共参数
     * @param message 消息
     * @param file    文件
     */
    private void sendPicMessageTemp(final Map<String, Object> mapTemp, final IMMessage message, final File file) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        pfb.addFile("datas", file.getName(), file);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                Log.i("123456789","response=="+response);
                String recode = "";
                String msg = "";
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    //TODO
                    if (obj.has("data")) {
                        org.json.JSONObject data = obj.getJSONObject("data");
                        if (data.has("chatId")) {
                            chatId = data.getString("chatId");
                        }
                    }
                    if ("000000".equals(recode)) {
                        mySendMessage(message);
                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void appendTeamMemberPush(IMMessage message) {
        if (aitManager == null) {
            return;
        }
        if (sessionType == SessionTypeEnum.Team) {
            List<String> pushList = aitManager.getAitTeamMember();
            if (pushList == null || pushList.isEmpty()) {
                return;
            }
            MemberPushOption memberPushOption = new MemberPushOption();
            memberPushOption.setForcePush(true);
            memberPushOption.setForcePushContent(message.getContent());
            memberPushOption.setForcePushList(pushList);
            message.setMemberPushOption(memberPushOption);
        }
    }

    private IMMessage changeToRobotMsg(IMMessage message) {
        if (aitManager == null) {
            return message;
        }
        if (message.getMsgType() == MsgTypeEnum.robot) {
            return message;
        }
        if (isChatWithRobot()) {
            if (message.getMsgType() == MsgTypeEnum.text && message.getContent() != null) {
                String content = message.getContent().equals("") ? " " : message.getContent();
                message = MessageBuilder.createRobotMessage(message.getSessionId(), message.getSessionType(), message.getSessionId(), content, RobotMsgType.TEXT, content, null, null);
            }
        } else {
            String robotAccount = aitManager.getAitRobot();
            if (TextUtils.isEmpty(robotAccount)) {
                return message;
            }
            String text = message.getContent();
            String content = aitManager.removeRobotAitString(text, robotAccount);
            content = content.equals("") ? " " : content;
            message = MessageBuilder.createRobotMessage(message.getSessionId(), message.getSessionType(), robotAccount, text, RobotMsgType.TEXT, content, null, null);

        }
        return message;
    }

    private boolean isChatWithRobot() {
        return NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(sessionId) != null;
    }

    private void appendPushConfig(IMMessage message) {
        CustomPushContentProvider customConfig = NimUIKitImpl.getCustomPushContentProvider();
        if (customConfig == null) {
            return;
        }
        String content = customConfig.getPushContent(message);
        Map<String, Object> payload = customConfig.getPushPayload(message);
        if (!TextUtils.isEmpty(content)) {
            message.setPushContent(content);
        }
        if (payload != null) {
            message.setPushPayload(payload);
        }

    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    @Override
    public void onItemFooterClick(IMMessage message) {
        if (aitManager == null) {
            return;
        }
        if (messageListPanel.isSessionMode()) {
            RobotAttachment attachment = (RobotAttachment) message.getAttachment();
            NimRobotInfo robot = NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(attachment.getFromRobotAccount());
            aitManager.insertAitRobot(robot.getAccount(), robot.getName(), inputPanel.getEditSelectionStart());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (aitManager != null) {
            aitManager.onActivityResult(requestCode, resultCode, data);
        }
        inputPanel.onActivityResult(requestCode, resultCode, data);
        messageListPanel.onActivityResult(requestCode, resultCode, data);
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        actions.add(new ImageAction());
        actions.add(new VideoAction());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actions.add(new AVChatAction(AVChatType.AUDIO));
            actions.add(new AVChatAction(AVChatType.VIDEO));
        }
        actions.add(new LocationAction());

        if (customization != null && customization.actions != null) {
            actions.addAll(customization.actions);
        }
        return actions;
    }

    protected MyDialog_AvChat dialog_avChat;

    protected void showWaitPage() {
        dialog_avChat = new MyDialog_AvChat(getActivity());
        dialog_avChat.setCanceledOnTouchOutside(false);
        dialog_avChat.setYesOnclickListener(new MyDialog_AvChat.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                dialog_avChat.dismiss();
                NimUIKit.setAccount("");
                NIMClient.getService(AuthService.class).logout();
                map.put("wkfFlag", "12");
                map.put("type", "");
                map.put("chat_id", chatId);
                Log.i("123456789","getDestoryAvChat=="+map.toString());
                getDestoryAvChat(map);
            }
        });
        dialog_avChat.show();
    }
    /**
     * 语音 视频通话结束
     *
     * @param mapTemp
     */
    private void getDestoryAvChat(final Map<String, Object> mapTemp) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                String recode = "";
                String msg = "";
                try {
                    Log.i("123456", "response==" + response);
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    if (obj.has("data")) {
                        org.json.JSONObject data = obj.getJSONObject("data");
                    }
                    if ("000000".equals(recode)) {
                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    protected MyDialog dialog;

    public void showDoLgoutDialog() {
        if ("".equals(chatId) || chatId == null) {
            NIMClient.getService(AuthService.class).logout();
            NimUIKit.setAccount("");
            getActivity().finish();
        }else{
            dialog = new MyDialog(getActivity());
            dialog.setTitle("提示");
            dialog.setMessage("是否结束会话？");
            dialog.setCanceledOnTouchOutside(true);
            dialog.setYesOnclickListener("确定", new MyDialog.onYesOnclickListener() {
                @Override
                public void onYesClick() {
                    dialog.dismiss();
                    Log.i("123456", "chatId==" + chatId);
                    map.put("wkfFlag", "11");
                    getScore(map);
                }
            });
            dialog.setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
                @Override
                public void onNoClick() {
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
            dialog.show();
        }
    }


    private String mScore = "";
    private List<ScoreBean> mList = new ArrayList<ScoreBean>();
    private MyDialog_zxzx dialog_zxzx;

    /**
     * 评价
     */
    private void showScoreDialog(final String type) {
        //score======="-1":"不满意","0":"一般","1":"基本满意","2":"满意","3":"非常满意"
        dialog_zxzx = new MyDialog_zxzx(getActivity());
        dialog_zxzx.setCanceledOnTouchOutside(true);
        dialog_zxzx.setmList(mList);
        dialog_zxzx.show();
        dialog_zxzx.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // TODO
                //mScore = Integer.parseInt(mList.get(position).getScore());
                mScore = mList.get(position).getScore();
                // 主要在mainactivity中使用
                //TODO 评分提交
                if ("".equals(chatId) || chatId == null) {
                    dialog_zxzx.dismiss();
                    ToastHelper.showToast(getActivity(), "您目前没有会话，无法进行评价！");
                } else {
                    dialog_zxzx.dismiss();
                    map.put("wkfFlag", "5");
                    map.put("type", "");
                    map.put("chat_id", chatId);
                    map.put("score", mScore);
                    sendScore(map, type);
                }
            }
        });
    }

    private void sendScore(final Map<String, Object> mapTemp, final String type) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                String recode = "";
                String msg = "";
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    if ("000000".equals(recode)) {
                        if ("1".equals(type)) {//1  推送评价
                            ToastHelper.showToast(getActivity(), "评价完成");
                            NimUIKit.setAccount("");
                            NIMClient.getService(AuthService.class).logout();
                            chatId = "";
                        } else {
                            map.put("wkfFlag", "12");
                            map.put("type", "");
                            map.put("chat_id", chatId);
                            Log.i("123456789","getDestoryChat=="+map.toString());
                            getDestoryChat(map);
                        }

                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 客户主动结束会话
     *
     * @param mapTemp
     */
    private void getDestoryChat(final Map<String, Object> mapTemp) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                String recode = "";
                String msg = "";
                try {
                    Log.i("123456", "response==" + response);
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    if (obj.has("data")) {
                        org.json.JSONObject data = obj.getJSONObject("data");
                    }
                    if ("000000".equals(recode)) {
                        getActivity().finish();
                        NimUIKit.setAccount("");
                        NIMClient.getService(AuthService.class).logout();
                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取评分模板
     *
     * @param mapTemp
     */
    private void getScore(final Map<String, Object> mapTemp) {
        pfb = OkHttpUtils.post().url(mUrl);
        pfb.params(mapTemp);
        RequestCall requestCall = pfb.build().connTimeOut(1000 * 300).writeTimeOut(300 * 1000).readTimeOut(300 * 1000);
        requestCall.execute(new StringCallback() {
            public void onError(Call call, Exception e) {
            }

            public void onResponse(String response) {
                String recode = "";
                String msg = "";
                try {
                    Log.i("123456", "response==" + response);
                    org.json.JSONObject obj = new org.json.JSONObject(response);
                    if (obj.has("recode")) {
                        recode = obj.getString("recode");
                    }
                    if (obj.has("msg")) {
                        msg = obj.getString("msg");
                    }
                    if ("000000".equals(recode)) {
                        if (obj.has("data")) {
                            org.json.JSONObject data = obj.getJSONObject("data");
                            if (data.has("scoreTemplate")) {
                                org.json.JSONArray scoreTemplate = data.getJSONArray("scoreTemplate");
                                mList = new ArrayList<ScoreBean>();
                                Gson gson = new Gson();
                                mList = gson.fromJson(scoreTemplate.toString(), new TypeToken<LinkedList<ScoreBean>>() {
                                }.getType());
                                if (isVisibility) {
                                    if (chatId != null || !"".equals(chatId)) {
                                        showScoreDialog("2");
                                    }
                                }
                            }

                        }

                    } else {
                        ToastHelper.showToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
