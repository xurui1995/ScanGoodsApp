package com.example.Receiver;

import com.example.activity.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.widget.Toast;
/**
 * 专门为首页面服务的广播接收者
 * @author dell
 *
 */
public class MReceiver extends BroadcastReceiver {
	private ConnectivityManager connectivityManager;
	private NetworkInfo info;
	private MainActivity mActivity;

	public MReceiver(MainActivity mActivity) {
		super();
		this.mActivity = mActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			info = connectivityManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				// 网络可用
				Message msg = new Message();
				msg.what = 1;
				// 返回UI线程，发送网络请求
				mActivity.handler.handleMessage(msg);

			} else { // 网络不可用
				Toast.makeText(context, "请检查网络", 0).show();

			}
		}
	}
}