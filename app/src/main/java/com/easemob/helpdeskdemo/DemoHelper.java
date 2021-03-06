package com.easemob.helpdeskdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.easemob.helpdeskdemo.receiver.CallReceiver;
import com.easemob.helpdeskdemo.ui.CallActivity;
import com.easemob.helpdeskdemo.ui.ChatActivity;
import com.easemob.helpdeskdemo.utils.ListenerManager;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.ChatManager;
import com.hyphenate.chat.Conversation;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.Message;
import com.hyphenate.chat.OfficialAccount;
import com.hyphenate.helpdesk.easeui.Notifier;
import com.hyphenate.helpdesk.easeui.UIProvider;
import com.hyphenate.helpdesk.easeui.util.CommonUtils;
import com.hyphenate.helpdesk.easeui.util.IntentBuilder;
import com.hyphenate.helpdesk.model.AgentInfo;
import com.hyphenate.helpdesk.model.MessageHelper;
import com.hyphenate.helpdesk.util.Log;
import com.hyphenate.push.EMPushConfig;
import com.heytap.mcssdk.PushManager;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;

import org.json.JSONObject;

import java.util.List;

public class DemoHelper {

    private static final String TAG = "DemoHelper";

    public static DemoHelper instance = new DemoHelper();

    /**
     * kefuChat.MessageListener
     */
    protected ChatManager.MessageListener messageListener = null;

    /**
     * ChatClient.ConnectionListener
     */
    private ChatClient.ConnectionListener connectionListener;

    private UIProvider _uiProvider;

    public boolean isVideoCalling;
    private CallReceiver callReceiver;
    private Context appContext;

    private DemoHelper(){}
    public synchronized static DemoHelper getInstance() {
        return instance;
    }

    /**
     * init helper
     *
     * @param context application context
     */
    public void init(final Context context) {
        appContext = context;
        ChatClient.Options options = new ChatClient.Options();
        options.setAppkey(Preferences.getInstance().getAppKey());
        options.setTenantId(Preferences.getInstance().getTenantId());
        options.showAgentInputState().showVisitorWaitCount().showMessagePredict();

        // ????????????????????????????????????????????????????????????????????????????????????
        EMPushConfig.Builder builder = new EMPushConfig.Builder(context);
        builder.enableVivoPush() // ?????????AndroidManifest.xml?????????appId???appKey
                .enableMeiZuPush("119943", "91163267c8784687804af6dd8e8fcf37")
                .enableMiPush("2882303761517507836", "5631750729836")
                .enableOppoPush("b08eb4a4b43f49799f45d136a5e2eabe", "52d5f8b887c14987bd306f6ffcd33044")
                .enableHWPush() // ?????????AndroidManifest.xml?????????appId
                .enableFCM("570662061026");

        options.setPushConfig(builder.build());
        //options.setKefuRestServer("https://sandbox.kefu.easemob.com");

	    //??????????????????????????????????????????????????????false??????????????????????????????
	    options.setConsoleLog(true);
//	    options.setUse2channel(true);
//        options.setAutoLogin(false);

        options.setAppVersion("1.2.7.1");

        // ???????????? SDK ?????????, ????????????????????????????????????????????????
        if (ChatClient.getInstance().init(context, options)){
            _uiProvider = UIProvider.getInstance();
            //?????????EaseUI
            _uiProvider.init(context);
            //??????easeui???api??????providers
            setEaseUIProvider(context);
            //??????????????????
            setGlobalListeners();

        }
    }



    private void setEaseUIProvider(final Context context){
        //????????????????????? ??????????????????????????????????????????????????????
        UIProvider.getInstance().setUserProfileProvider(new UIProvider.UserProfileProvider() {
            @Override
            public void setNickAndAvatar(Context context, Message message, ImageView userAvatarView, TextView usernickView) {
                if (message.direct() == Message.Direct.RECEIVE) {
                    //??????????????????????????????
                    AgentInfo agentInfo = MessageHelper.getAgentInfo(message);
                    OfficialAccount officialAccount = message.getOfficialAccount();
                    if (usernickView != null){
                        usernickView.setText(message.from());
                        if (agentInfo != null){
                            if (!TextUtils.isEmpty(agentInfo.getNickname())) {
                                usernickView.setText(agentInfo.getNickname());
                            }
                        }
                    }
                    if (userAvatarView != null){
                        userAvatarView.setImageResource(com.hyphenate.helpdesk.R.drawable.hd_default_avatar);

                        // ?????????????????????????????????????????????logo??????
                        if (officialAccount != null) {
                            if (!TextUtils.isEmpty(officialAccount.getImg())) {
                                String imgUrl = officialAccount.getImg();
                                // ??????????????????
                                if (!TextUtils.isEmpty(imgUrl)) {
                                    if (!imgUrl.startsWith("http")) {
                                        imgUrl = "http:" + imgUrl;
                                    }
                                    //?????????string??????
                                    Glide.with(context).load(imgUrl).apply(RequestOptions.placeholderOf(com.hyphenate.helpdesk.R.drawable.hd_default_avatar).diskCacheStrategy(DiskCacheStrategy.ALL)).into(userAvatarView);
                                }
                            }
                        }

                        if (agentInfo != null){
                            if (!TextUtils.isEmpty(agentInfo.getAvatar())) {
                                String strUrl = agentInfo.getAvatar();
                                // ??????????????????
                                if (!TextUtils.isEmpty(strUrl)) {
                                    if (!strUrl.startsWith("http")) {
                                        strUrl = "http:" + strUrl;
                                    }
                                    //?????????string??????
                                    Glide.with(context).load(strUrl).apply(RequestOptions.placeholderOf(com.hyphenate.helpdesk.R.drawable.hd_default_avatar).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(userAvatarView);
                                    return;
                                }
                            }
                        }
                    }
                } else {
                    //??????????????????????????????????????????
                    if (userAvatarView != null){
                        userAvatarView.setImageResource(R.drawable.hd_default_avatar);
//                        Glide.with(context).load("http://oev49clxj.bkt.clouddn.com/7a8aed7bjw1f32d0cumhkj20ey0mitbx.png").diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.hd_default_avatar).into(userAvatarView);
//                        ??????????????????????????????????????????http://blog.csdn.net/weidongjian/article/details/47144549
                    }
                }
            }
        });


        //?????????????????????
        _uiProvider.getNotifier().setNotificationInfoProvider(new Notifier.NotificationInfoProvider() {
            @Override
            public String getTitle(Message message) {
                //????????????,??????????????????
                return null;
            }

            @Override
            public int getSmallIcon(Message message) {
                //?????????????????????????????????
                return 0;
            }

            @Override
            public String getDisplayedText(Message message) {
                // ?????????????????????????????????????????????message????????????????????????
                String ticker = CommonUtils.getMessageDigest(message, context);
                if (message.getType() == Message.Type.TXT) {
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", context.getString(R.string.noti_text_expression));
                }
                return message.from() + ": " + ticker;
            }

            @Override
            public String getLatestText(Message message, int fromUsersNum, int messageNum) {
                return null;
                // return fromUsersNum + "contacts send " + messageNum + "messages to you";
            }

            @Override
            public Intent getLaunchIntent(Message message) {
                Intent intent;
                if (isVideoCalling){
                    intent = new Intent(context, CallActivity.class);
                }else{
                    //?????????????????????????????????
                    Conversation conversation = ChatClient.getInstance().chatManager().getConversation(message.from());
                    String titleName = null;
                    if (conversation.officialAccount() != null){
                        titleName = conversation.officialAccount().getName();
                    }
                    intent = new IntentBuilder(context)
                            .setTargetClass(ChatActivity.class)
                            .setServiceIMNumber(conversation.conversationId())
                            .setVisitorInfo(DemoMessageHelper.createVisitorInfo())
                            .setTitleName(titleName)
                            .setShowUserNick(true)
                            .build();

                }
                return intent;
            }
        });

        //?????????,???????????????, ?????????????????????
//        _uiProvider.setSettingsProvider(new UIProvider.SettingsProvider() {
//            @Override
//            public boolean isMsgNotifyAllowed(Message message) {
//                return false;
//            }
//
//            @Override
//            public boolean isMsgSoundAllowed(Message message) {
//                return false;
//            }
//
//            @Override
//            public boolean isMsgVibrateAllowed(Message message) {
//                return false;
//            }
//
//            @Override
//            public boolean isSpeakerOpened() {
//                return false;
//            }
//        });
//        ChatClient.getInstance().getChat().addMessageListener(new MessageListener() {
//            @Override
//            public void onMessage(List<Message> msgs) {
//
//            }
//
//            @Override
//            public void onCmdMessage(List<Message> msgs) {
//
//            }
//
//            @Override
//            public void onMessageSent() {
//
//            }
//
//            @Override
//            public void onMessageStatusUpdate() {
//
//            }
//        });
    }


    private void setGlobalListeners(){
        // create the global connection listener
        /*connectionListener = new ChatClient.ConnectionListener(){

            @Override
            public void onConnected() {
                //onConnected
            }

            @Override
            public void onDisconnected(int errorcode) {
                if (errorcode == Error.USER_REMOVED){
                    //???????????????
                }else if (errorcode == Error.USER_LOGIN_ANOTHER_DEVICE){
                    //???????????????????????????
                }
            }
        };

        //??????????????????
        ChatClient.getInstance().addConnectionListener(connectionListener);*/

        //????????????????????????
        registerEventListener();

        IntentFilter callFilter = new IntentFilter(ChatClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null){
            callReceiver = new CallReceiver();
        }
        // register incoming call receiver
        appContext.registerReceiver(callReceiver, callFilter);
    }

    /**
     * ??????????????????
     * ??????????????????UI???????????????????????????????????????????????????UI???????????????????????????????????????????????????
     * activityList.size() <= 0 ??????????????????????????????????????????????????????????????????Activity Stack
     */
    protected void registerEventListener(){
        messageListener = new ChatManager.MessageListener(){

            @Override
            public void onMessage(List<Message> msgs) {
                for (Message message : msgs){
                    Log.d(TAG, "onMessageReceived id : " + message.messageId());
//
                    //?????????????????????????????????,???????????????????????????????????????????????????
                    if (MessageHelper.isNotificationMessage(message)){
                        // ????????????????????????????????????
                        String eventName = getEventNameByNotification(message);
                        if (!TextUtils.isEmpty(eventName)){
                            if (eventName.equals("TicketStatusChangedEvent") || eventName.equals("CommentCreatedEvent")){
                                // ???????????????????????????????????????,??????????????????
                                JSONObject jsonTicket = null;
                                try{
                                    jsonTicket = message.getJSONObjectAttribute("weichat").getJSONObject("event").getJSONObject("ticket");
                                }catch (Exception ignored){}
                                ListenerManager.getInstance().sendBroadCast(eventName, jsonTicket);
                            }
                        }
                    } else if (message.isNeedToScore()){
                        MessageHelper.createInviteCommentMsg(message, "");
                        message.setIsNeedToScore(false);
                    }
                }
            }

            @Override
            public void onCmdMessage(List<Message> msgs) {
                for (Message message : msgs){
                    Log.d(TAG, "??????????????????");
                    //????????????body
                    EMCmdMessageBody cmdMessageBody = (EMCmdMessageBody) message.body();
                    String action = cmdMessageBody.action(); //???????????????action
                    Log.d(TAG, String.format("????????????: action:%s,message:%s", action, message.toString()));
                }
            }

            @Override
            public void onMessageStatusUpdate() {

            }

            @Override
            public void onMessageSent() {

            }
        };

        ChatClient.getInstance().chatManager().addMessageListener(messageListener);
    }


    /**
     * ??????EventName
     * @param message
     * @return
     */
    public String getEventNameByNotification(Message message){

        try {
            JSONObject weichatJson = message.getJSONObjectAttribute("weichat");
            if (weichatJson != null && weichatJson.has("event")) {
                JSONObject eventJson = weichatJson.getJSONObject("event");
                if (eventJson != null && eventJson.has("eventName")){
                    return eventJson.getString("eventName");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void pushActivity(Activity activity){
        _uiProvider.pushActivity(activity);
    }

    public void popActivity(Activity activity){
        _uiProvider.popActivity(activity);
    }

    public Notifier getNotifier(){
        return _uiProvider.getNotifier();
    }

    /**
     * ????????????????????????
     */
    public void showNotificationPermissionDialog() {
        EMPushType pushType = EMPushHelper.getInstance().getPushType();
        // oppo
        if(pushType == EMPushType.OPPOPUSH && PushManager.isSupportPush(appContext)) {
            PushManager.getInstance().requestNotificationPermission();
        }
    }
}
