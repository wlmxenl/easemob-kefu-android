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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdeskdemo.Constant;
import com.easemob.helpdeskdemo.Preferences;
import com.easemob.helpdeskdemo.R;
import com.easemob.helpdeskdemo.utils.ListenerManager;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.helpdesk.callback.Callback;
import com.hyphenate.helpdesk.easeui.widget.ToastHelper;
//import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import cn.bertsir.zbar.QrConfig;
import cn.bertsir.zbar.QrManager;

public class SettingFragment extends Fragment implements View.OnClickListener{

	private RelativeLayout rlAppkey;
	private RelativeLayout rlAccount;
	private RelativeLayout rlNick;
	private RelativeLayout rlTenantId;
	private RelativeLayout rlProjectId;
	private RelativeLayout rlQcode;

	private TextView tvAppkey;
	private TextView tvAccount;
	private TextView tvNick;
	private TextView tvTenantId;
	private TextView tvProjectId;
	private TextView tvVersion;


	private static final int REQUEST_CODE_APPKEY = 1;
	private static final int REQUEST_CODE_ACCOUNT = 2;
	private static final int REQUEST_CODE_NICK = 3;
	private static final int REQUEST_CODE_TENANT_ID = 4;
	private static final int REQUEST_CODE_PROJECT_ID = 5;

	private Dialog dialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.em_setting_fragment, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
		initListener();
		try {
			tvVersion.setText("v" + ChatClient.getInstance().sdkVersion() + "(" + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode +")");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}


	}
	private void initView() {
		if (getView() == null){
			return;
		}
		tvAppkey = (TextView) getView().findViewById(R.id.tv_setting_appkey);
		tvAccount = (TextView) getView().findViewById(R.id.tv_setting_account);
		tvNick = (TextView) getView().findViewById(R.id.tv_setting_nick);
		tvTenantId = (TextView) getView().findViewById(R.id.tv_setting_tenant_id);
		tvProjectId = (TextView) getView().findViewById(R.id.tv_setting_project_id);
		tvVersion = (TextView) getView().findViewById(R.id.tv_version);

		rlAppkey = (RelativeLayout) getView().findViewById(R.id.ll_setting_list_appkey);
		rlAccount = (RelativeLayout) getView().findViewById(R.id.ll_setting_list_account);
		rlNick = (RelativeLayout) getView().findViewById(R.id.ll_setting_list_nick);
		rlTenantId = (RelativeLayout) getView().findViewById(R.id.ll_setting_tenant_id);
		rlProjectId = (RelativeLayout) getView().findViewById(R.id.ll_setting_project_id);
		rlQcode = (RelativeLayout) getView().findViewById(R.id.rl_qcode);
	}

	private void initListener() {
		tvAppkey.setText(Preferences.getInstance().getAppKey());
		tvAccount.setText(Preferences.getInstance().getCustomerAccount());
		tvNick.setText(Preferences.getInstance().getNickName());
		tvTenantId.setText(Preferences.getInstance().getTenantId());
		tvProjectId.setText(Preferences.getInstance().getProjectId());

		rlAppkey.setOnClickListener(this);
		rlAccount.setOnClickListener(this);
		rlNick.setOnClickListener(this);
		rlTenantId.setOnClickListener(this);
		rlProjectId.setOnClickListener(this);
		rlQcode.setOnClickListener(this);
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQUEST_CODE_APPKEY:
					String oldAppkey = tvAppkey.getText().toString();
					String newAppkey = data.getStringExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT);
					if (TextUtils.isEmpty(newAppkey.trim())) {
						ToastHelper.show(getActivity(), R.string.app_key_cannot_be_empty);
						return;
					}
					if (!newAppkey.matches("^[0-9a-zA-Z-_#]+$")) {
						ToastHelper.show(getActivity(), R.string.app_key_format_wrong);
						return;
					}

					if (oldAppkey.equals(newAppkey)) {
						return;
					}
					tvAppkey.setText(newAppkey);
					showCustomMessage(newAppkey);
					break;
				case REQUEST_CODE_ACCOUNT:
					String oldAccount = tvAccount.getText().toString();
					String newAccount = data.getStringExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT);
					if (TextUtils.isEmpty(newAccount.trim())) {
						ToastHelper.show(getActivity(), R.string.cus_account_cannot_be_empty);
						return;
					}
					if (oldAccount.equals(newAccount)) {
						return;
					}
					tvAccount.setText(newAccount);
					Preferences.getInstance().setCustomerAccount(newAccount);
					break;
				case REQUEST_CODE_NICK:
					String oldNick = tvNick.getText().toString();
					String newNick = data.getStringExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT);
					if (oldNick.equals(newNick)) {
						return;
					}
					tvNick.setText(newNick);
					Preferences.getInstance().setNickName(newNick);
					break;
				case REQUEST_CODE_TENANT_ID:
					String oldTenantId = tvTenantId.getText().toString();
					String newTenantId = data.getStringExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT);
					if (!TextUtils.isDigitsOnly(newTenantId)) {
						return;
					}
					if (oldTenantId.equals(newTenantId)) {
						return;
					}
					tvTenantId.setText(newTenantId);
					Preferences.getInstance().setTenantId(newTenantId);
					ChatClient.getInstance().changeTenantId(newTenantId);
					break;
				case REQUEST_CODE_PROJECT_ID:
					String oldProjectId = tvProjectId.getText().toString();
					String newProjectId = data.getStringExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT);
					if (!TextUtils.isDigitsOnly(newProjectId)) {
						return;
					}
					if (oldProjectId.equals(newProjectId)) {
						return;
					}
					tvProjectId.setText(newProjectId);
					Preferences.getInstance().setSettingProjectId(newProjectId);
					break;
				default:
					break;
			}
		}
	}

	public static Map<String, String> urlParamParse(String url) {
		Map<String, String> mapRequest = new HashMap<String, String>();
		String[] arrSplit = null;
		String strUrlParam = url.substring(url.indexOf('?') + 1);
		try {
			strUrlParam = URLDecoder.decode(strUrlParam, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		strUrlParam = strUrlParam.toLowerCase();
		arrSplit = strUrlParam.split("&");
		for (String strSplite : arrSplit) {
			String[] arrSpliteEqual = null;
			arrSpliteEqual = strSplite.split("=");
			if (arrSpliteEqual.length > 1) {
				mapRequest.put(arrSpliteEqual[0], arrSpliteEqual[1]);
			} else {
				if (!TextUtils.isEmpty(arrSpliteEqual[0])) {
					mapRequest.put(arrSpliteEqual[0], "");
				}
			}
		}
		return mapRequest;
	}

	private void showCustomMessage(final String newAppkey) {
		Preferences.getInstance().setAppKey(newAppkey);
		ListenerManager.getInstance().sendBroadCast("clearTicketEvent", null);
		if (!ChatClient.getInstance().isLoggedInBefore()){
			changeAppKey(newAppkey);
		}else{
			//?????????????????? (????????????????????????appkey)
			ChatClient.getInstance().logout(true, new Callback() {
				@Override
				public void onSuccess() {
					changeAppKey(newAppkey);
				}

				@Override
				public void onError(int i, String s) {
					ChatClient.getInstance().logout(false, new Callback() {
						@Override
						public void onSuccess() {
							changeAppKey(newAppkey);
						}
						@Override
						public void onError(int i, String s) {
						}
						@Override
						public void onProgress(int i, String s) {
						}
					});
				}
				@Override
				public void onProgress(int i, String s) {

				}
			});
		}


	}

	/**
	 * ????????????appkey,???????????????????????????,?????????????????????
	 * @param appkey
	 */
	private void changeAppKey(final String appkey){
		if (getActivity() == null){
			return;
		}
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					ChatClient.getInstance().changeAppKey(appkey);
				} catch (HyphenateException e) {
					ToastHelper.show(getActivity(), R.string.app_key_modify_fail);
				}
			}
		});
	}


	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
			case R.id.ll_setting_list_appkey:
				String strAppkey = tvAppkey.getText().toString();
				intent.setClass(getActivity(), ModifyActivity.class);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_INDEX, Constant.MODIFY_INDEX_APPKEY);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT, strAppkey);
				startActivityForResult(intent, REQUEST_CODE_APPKEY);
				break;
			case R.id.ll_setting_list_account:
				String strAccount = tvAccount.getText().toString();
				intent.setClass(getActivity(), ModifyActivity.class);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_INDEX, Constant.MODIFY_INDEX_ACCOUNT);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT, strAccount);
				startActivityForResult(intent, REQUEST_CODE_ACCOUNT);
				break;
			case R.id.ll_setting_list_nick:
				String strNick = tvNick.getText().toString();
				intent.setClass(getActivity(), ModifyActivity.class);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_INDEX, Constant.MODIFY_INDEX_NICK);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT, strNick);
				startActivityForResult(intent, REQUEST_CODE_NICK);
				break;
			case R.id.ll_setting_tenant_id:
				String strTenantId = tvTenantId.getText().toString();
				intent.setClass(getActivity(), ModifyActivity.class);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_INDEX, Constant.MODIFY_INDEX_TENANT_ID);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT, strTenantId);
				startActivityForResult(intent, REQUEST_CODE_TENANT_ID);
				break;
			case R.id.ll_setting_project_id:
				String strProjectId = tvProjectId.getText().toString();
				intent.setClass(getActivity(), ModifyActivity.class);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_INDEX, Constant.MODIFY_INDEX_PROJECT_ID);
				intent.putExtra(Constant.MODIFY_ACTIVITY_INTENT_CONTENT, strProjectId);
				startActivityForResult(intent, REQUEST_CODE_PROJECT_ID);
				break;
			case R.id.rl_qcode:
				QrConfig qrConfig = new QrConfig.Builder()
						.setDesText("???????????????") //??????????????????
						.setShowDes(false)//?????????????????????????????????
						.setShowLight(true) //?????????????????????
						.setShowTitle(true)//??????Title
						.setShowAlbum(false) //???????????????????????????
						.setCornerColor(Color.WHITE)//?????????????????????
						.setLineColor(Color.WHITE)//?????????????????????
						.setLineSpeed(QrConfig.LINE_MEDIUM)//?????????????????????
						.setScanType(QrConfig.TYPE_QRCODE)//????????????????????????????????????????????????
						.setPlaySound(true)//?????????????????????bi~?????????
						.setTitleText("???????????????")//??????Tilte??????
						.setTitleBackgroudColor(getResources().getColor(R.color.title_bg_color))//?????????????????????
						.setTitleTextColor(Color.WHITE)//??????Title????????????
						.create();
				QrManager.getInstance().init(qrConfig).startScan(getActivity(), new QrManager.OnScanResultCallback() {
					@Override
					public void onScanSuccess(String result) {
						dealWithQrcodeResult(result);
					}

					@Override
					public void onScanFail(int errorCode) {
						ToastHelper.show(getActivity(), R.string.qrcode_permission_fail);
					}
				});
				break;
			default:
				break;
		}

	}


	private void dealWithQrcodeResult(String result) {
		//??????????????????
		try {
			Map<String, String> paramMap = urlParamParse(result);
			String appkey = paramMap.get("appkey");
			String imServiceNum = paramMap.get("imservicenum");
			String tenantId = paramMap.get("tenantid");
			String projectId = paramMap.get("projectid");
			if (!TextUtils.isEmpty(appkey)) {
				tvAppkey.setText(appkey);
				showCustomMessage(appkey);
			}
			if (!TextUtils.isEmpty(projectId)) {
				tvProjectId.setText(projectId);
				Preferences.getInstance().setSettingProjectId(projectId);
			}
			if (!TextUtils.isEmpty(tenantId)) {
				tvTenantId.setText(tenantId);
				Preferences.getInstance().setTenantId(tenantId);
				ChatClient.getInstance().changeTenantId(tenantId);
			}
			if (!TextUtils.isEmpty(imServiceNum)) {
				tvAccount.setText(imServiceNum);
				Preferences.getInstance().setCustomerAccount(imServiceNum);
			}
			if (!TextUtils.isEmpty(appkey) && !TextUtils.isEmpty(tenantId)) {
				ToastHelper.show(getActivity(), R.string.qrcode_success);
			} else {
				ToastHelper.show(getActivity(), R.string.qrcode_invalid);
			}
		} catch (Exception e) {
			ToastHelper.show(getActivity(), R.string.qrcode_fail);
		}
	}

	private void closeDialog(){
		if (dialog != null && dialog.isShowing()){
			dialog.dismiss();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		closeDialog();
	}
}
