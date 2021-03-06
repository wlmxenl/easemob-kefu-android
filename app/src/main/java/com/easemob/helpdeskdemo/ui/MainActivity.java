/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.helpdeskdemo.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.easemob.bottomnavigation.BottomNavigation;
import com.easemob.bottomnavigation.OnBottomNavigationSelectedListener;
import com.easemob.helpdeskdemo.Constant;
import com.easemob.helpdeskdemo.DemoHelper;
import com.easemob.helpdeskdemo.HMSPushHelper;
import com.easemob.helpdeskdemo.R;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.ChatManager;
import com.hyphenate.chat.Message;
import com.hyphenate.helpdesk.Error;
import com.hyphenate.helpdesk.easeui.runtimepermission.PermissionsManager;
import com.hyphenate.helpdesk.easeui.runtimepermission.PermissionsResultAction;
import com.hyphenate.util.EasyUtils;

import java.util.List;

public class MainActivity extends DemoBaseActivity implements OnBottomNavigationSelectedListener {

    private Fragment shopFragment = null;
    private Fragment settingFragment = null;
    private Fragment ticketListFragment = null;
    private Fragment conversationsFragment = null;
    private Fragment[] fragments;
    private int currentTabIndex = 0;
    private MyConnectionListener connectionListener = null;
    private BottomNavigation mBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            try {
                assert pm != null;
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                }
            } catch (Exception ignored) {
                //?????????????????????????????????Activity
                //android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS dat=package:com.easemob.helpdeskdemo }
            }

        }

        setContentView(R.layout.em_activity_main);

        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();

        if (savedInstanceState != null){
            currentTabIndex = savedInstanceState.getInt("selectedIndex", 0);
            //Activity?????????????????????????????????Fragment????????????
            if (shopFragment == null){
                shopFragment = getSupportFragmentManager().findFragmentByTag("shopFragment");
                settingFragment = getSupportFragmentManager().findFragmentByTag("settingFragment");
                ticketListFragment = getSupportFragmentManager().findFragmentByTag("ticketListFragment");
                conversationsFragment = getSupportFragmentManager().findFragmentByTag("conversationsFragment");
            }
        }

        if (shopFragment == null) {
            shopFragment = new ShopFragment();
            trx.add(R.id.fragment_container, shopFragment, "shopFragment");
        }

        if (ticketListFragment == null) {
            ticketListFragment = new TicketListFragment();
            trx.add(R.id.fragment_container, ticketListFragment, "ticketListFragment");
        }

        if (conversationsFragment == null) {
            conversationsFragment = new ConversationListFragment();
            trx.add(R.id.fragment_container, conversationsFragment, "conversationsFragment");
        }

        if (settingFragment == null) {
            settingFragment = new SettingFragment();
            trx.add(R.id.fragment_container, settingFragment, "settingFragment");
        }

        fragments = new Fragment[]{shopFragment, ticketListFragment, conversationsFragment, settingFragment};

        // ???shopFragment??????????????????
        trx.hide(settingFragment)
           .hide(ticketListFragment)
           .hide(conversationsFragment)
           .hide(shopFragment)
           .show(fragments[currentTabIndex])
           .commit();


        mBottomNav = $(R.id.bottom_navigation);
        mBottomNav.setBottomNavigationSelectedListener(this);
        //?????????????????????????????????listener
        connectionListener = new MyConnectionListener();
        ChatClient.getInstance().addConnectionListener(connectionListener);
        //6.0????????????????????????target api??????23??????demo?????????????????????????????????????????????????????????????????????
        requestPermissions();


        // ????????????????????????
        HMSPushHelper.getInstance().getHMSToken(this);
    }

    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(String permission) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }


    @Override
    public void onValueSelected(int index) {
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commitAllowingStateLoss();
        }
        currentTabIndex = index;
    }

    public class MyConnectionListener implements ChatClient.ConnectionListener {

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected(final int errorCode) {
            if (errorCode == Error.USER_NOT_FOUND || errorCode == Error.USER_LOGIN_ANOTHER_DEVICE
                    || errorCode == Error.USER_AUTHENTICATION_FAILED
                    || errorCode == Error.USER_REMOVED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //demo??????????????????????????????????????????????????????????????????,??????????????????
                        //??????APP??????????????????????????????????????????
                        if (ChatActivity.instance != null) {
                            ChatActivity.instance.finish();
                        }
                        ChatClient.getInstance().logout(false, null);
                    }
                });
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void contactCustomer(View view) {
        switch (view.getId()) {
            case R.id.ll_setting_list_customer:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                intent.putExtra(Constant.MESSAGE_TO_INTENT_EXTRA, Constant.MESSAGE_TO_DEFAULT);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionListener != null) {
            ChatClient.getInstance().removeConnectionListener(connectionListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DemoHelper.getInstance().pushActivity(this);
        ChatClient.getInstance().chatManager().addMessageListener(messageListener);
        DemoHelper.getInstance().showNotificationPermissionDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedIndex", currentTabIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentTabIndex = savedInstanceState.getInt("selectedIndex", 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ChatClient.getInstance().chatManager().removeMessageListener(messageListener);
        DemoHelper.getInstance().popActivity(this);

    }

    ChatManager.MessageListener messageListener = new ChatManager.MessageListener() {

        @Override
        public void onMessage(final List<Message> msgs) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //????????????????????????UI???
//                    int unreadMsgCount = ChatClient.getInstance().chatManager().getUnreadMsgsCount();

                    if (EasyUtils.isAppRunningForeground(MainActivity.this)){
                        DemoHelper.getInstance().getNotifier().onNewMesg(msgs);
                    }

                    if (conversationsFragment != null){
                        ((ConversationListFragment)conversationsFragment).refresh();
                    }
                }
            });
        }

        @Override
        public void onCmdMessage(List<Message> msgs) {

        }

        @Override
        public void onMessageStatusUpdate() {

        }

        @Override
        public void onMessageSent() {

        }
    };

}

