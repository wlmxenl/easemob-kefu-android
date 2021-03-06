/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.helpdeskdemo.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.easemob.helpdeskdemo.Constant;
import com.easemob.helpdeskdemo.DemoMessageHelper;
import com.easemob.helpdeskdemo.HMSPushHelper;
import com.easemob.helpdeskdemo.Preferences;
import com.easemob.helpdeskdemo.R;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.Conversation;
import com.hyphenate.helpdesk.Error;
import com.hyphenate.helpdesk.callback.Callback;
import com.hyphenate.helpdesk.easeui.util.IntentBuilder;
import com.hyphenate.helpdesk.easeui.widget.ToastHelper;


public class LoginActivity extends DemoBaseActivity {

	private static final String TAG = "LoginActivity";

	private boolean progressShow;
	private ProgressDialog progressDialog;
	private int selectedIndex = Constant.INTENT_CODE_IMG_SELECTED_DEFAULT;
	private int messageToIndex = Constant.MESSAGE_TO_DEFAULT;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Intent intent = getIntent();
		selectedIndex = intent.getIntExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY,
				Constant.INTENT_CODE_IMG_SELECTED_DEFAULT);
		messageToIndex = intent.getIntExtra(Constant.MESSAGE_TO_INTENT_EXTRA, Constant.MESSAGE_TO_DEFAULT);
		
		//ChatClient.getInstance().isLoggedInBefore() ??????????????????????????????????????????????????????????????????SDK???????????????????????????????????????????????????
		if (ChatClient.getInstance().isLoggedInBefore()) {
			progressDialog = getProgressDialog();
			progressDialog.setMessage(getResources().getString(R.string.is_contact_customer));
			progressDialog.show();
			toChatActivity();
		} else {
			//????????????????????????????????????????????????
			createRandomAccountThenLoginChatServer();
		}

	}


	private void createRandomAccountThenLoginChatServer() {
		// ??????????????????,???????????????????????????????????????,????????????.???????????????????????????????????????
		final String account = Preferences.getInstance().getUserName();
		final String userPwd = Constant.DEFAULT_ACCOUNT_PWD;
		progressDialog = getProgressDialog();
		progressDialog.setMessage(getString(R.string.system_is_regist));
		progressDialog.show();
		// createAccount to huanxin server
		// if you have a account, this step will ignore
		ChatClient.getInstance().register(account, userPwd, new Callback() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "demo register success");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//?????????????????????
						login(account, userPwd);
					}
				});
			}

			@Override
			public void onError(final int errorCode, String error) {
				runOnUiThread(new Runnable() {
					public void run() {
						if(progressDialog != null && progressDialog.isShowing()){
							progressDialog.dismiss();
						}
						if (errorCode == Error.NETWORK_ERROR){
							ToastHelper.show(getBaseContext(), R.string.network_unavailable);
						}else if (errorCode == Error.USER_ALREADY_EXIST){
							ToastHelper.show(getBaseContext(), R.string.user_already_exists);
						}else if(errorCode == Error.USER_AUTHENTICATION_FAILED){
							ToastHelper.show(getBaseContext(), R.string.no_register_authority);
						} else if (errorCode == Error.USER_ILLEGAL_ARGUMENT){
							ToastHelper.show(getBaseContext(), R.string.illegal_user_name);
						}else {
							ToastHelper.show(getBaseContext(), R.string.register_user_fail);
						}
						finish();
					}
				});
			}

			@Override
			public void onProgress(int progress, String status) {

			}
		});
	}

	private ProgressDialog getProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					progressShow = false;
				}
			});
		}
		return progressDialog;
	}

	private void login(final String uname, final String upwd) {
		progressShow = true;
		progressDialog = getProgressDialog();
		progressDialog.setMessage(getResources().getString(R.string.is_contact_customer));
		if (!progressDialog.isShowing()) {
			if (isFinishing()){
				return;
			}
			progressDialog.show();
		}
		// login huanxin server
		ChatClient.getInstance().login(uname, upwd, new Callback() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "demo login success!");
				if (!progressShow) {
					return;
				}
				toChatActivity();
			}

			@Override
			public void onError(int code, String error) {
				Log.e(TAG, "login fail,code:" + code + ",error:" + error);
				if (!progressShow) {
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						progressDialog.dismiss();
						ToastHelper.show(getBaseContext(), R.string.is_contact_customer_failure_seconed);
						finish();
					}
				});
			}

			@Override
			public void onProgress(int progress, String status) {

			}
		});
	}

	private void toChatActivity() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!LoginActivity.this.isFinishing())
					progressDialog.dismiss();

				// ???????????? HMS ?????? token
				HMSPushHelper.getInstance().getHMSToken(LoginActivity.this);

				//???????????????????????????,???????????????????????????????????????[shouqian|shouhou],????????????????????????????????????.
				//???null???????????????????????????,????????????????????????????????????scheduleAgent
				String queueName = null;
				switch (messageToIndex){
					case Constant.MESSAGE_TO_AFTER_SALES:
						queueName = "shouhou";
						break;
					case Constant.MESSAGE_TO_PRE_SALES:
						queueName = "shouqian";
						break;
					default:
						break;
				}
				Bundle bundle = new Bundle();
				bundle.putInt(Constant.INTENT_CODE_IMG_SELECTED_KEY, selectedIndex);
			 //?????????????????????????????????
				Conversation conversation = ChatClient.getInstance().chatManager().getConversation(Preferences.getInstance().getCustomerAccount());
				String titleName = null;
				if (conversation.officialAccount() != null){
					titleName = conversation.officialAccount().getName();
				}
				// ???????????????
				Intent intent = new IntentBuilder(LoginActivity.this)
						.setTargetClass(ChatActivity.class)
						.setVisitorInfo(DemoMessageHelper.createVisitorInfo())
						.setServiceIMNumber(Preferences.getInstance().getCustomerAccount())
						.setScheduleQueue(DemoMessageHelper.createQueueIdentity(queueName))
						.setTitleName(titleName)
//						.setScheduleAgent(DemoMessageHelper.createAgentIdentity("ceshiok1@qq.com"))
						.setShowUserNick(true)
						.setBundle(bundle)
						.build();
				startActivity(intent);
				finish();

			}
		});
	}

}
