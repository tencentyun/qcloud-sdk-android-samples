package com.tencent.tac;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.tencent.qcloud.core.http.HttpRequest;
import com.tencent.qcloud.core.logger.QCloudLogger;
import com.tencent.tac.analytics.TACAnalyticsOptions;
import com.tencent.tac.analytics.TACAnalyticsService;
import com.tencent.tac.analytics.TACAnalyticsStrategy;
import com.tencent.tac.crash.TACCrashHandleCallback;
import com.tencent.tac.crash.TACCrashOptions;
import com.tencent.tac.crash.TACCrashService;
import com.tencent.tac.messaging.TACMessagingOptions;
import com.tencent.tac.messaging.TACMessagingService;
import com.tencent.tac.option.TACApplicationOptions;
import com.tencent.tac.payment.TACPaymentOptions;
import com.tencent.tac.storage.TACStorageOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("app", "app create");

		initTACApplication(true);
		TACApplicationOptions applicationOptions = TACApplication.options();

		initAnalytics(applicationOptions);
		initCrash(applicationOptions);
		initMessaging(applicationOptions);
		initPayment(applicationOptions);
		initStorage(applicationOptions);
	}

	/**
	 * 全局服务配置
	 *
	 *
	 * @param isDebug
	 * @return
	 */
	private void initTACApplication(boolean isDebug) {

		if (isDebug) {
			TACApplicationOptions applicationOptions = TACApplicationOptions.newDefaultOptions(this);
			// change some settings.
			TACApplication.configureWithOptions(this, applicationOptions);
		} else {
			TACApplication.configure(this);
		}
	}
	
	/**
	 * Analytics 服务初始化
	 */
	private void initAnalytics(TACApplicationOptions applicationOptions) {

		TACAnalyticsOptions analyticsOptions = applicationOptions.sub("analytics");
		analyticsOptions.strategy(TACAnalyticsStrategy.INSTANT); // 立即发送
		TACAnalyticsService.getInstance().start(this);
	}


	/**
	 * Crash 服务初始化
	 *
	 * @param applicationOptions
	 */
	private void initCrash(TACApplicationOptions applicationOptions) {


		TACCrashOptions crashOptions = applicationOptions.sub("crash");
		//crashOptions.enableUserInfo(false);
		crashOptions.setHandleCallback(new TACCrashHandleCallback() {
			@Override
			public Map<String, String> onCrashUploadKeyValues(int crashType, String errorCode, String errorMessage, String errorStack) {
				QCloudLogger.d("crash", "onCrashUploadKeyValues");
				Map<String, String> map = new HashMap<>();
				map.put("key", "crash values");
				return map;
			}

			@Override
			public byte[] onCrashUploadBinary(int crashType, String errorCode, String errorMessage, String errorStack) {
				QCloudLogger.d("crash", "onCrashUploadBinary");
				byte[] bin = "this is upload binary".getBytes();
				return bin;
			}
		});

		if (isMainProcess()) {
			TACCrashService.getInstance().start(this);
		}
	}

	/**
	 * Messaging 服务初始化
	 *
	 * @param applicationOptions
	 */
	private void initMessaging(TACApplicationOptions applicationOptions) {

		QCloudLogger.d("messaging", "init messaging");
		TACMessagingOptions messagingOptions = applicationOptions.sub("messaging");
		if (isMainProcess()) {
			QCloudLogger.d("messaging", "start receive notification");
			TACMessagingService.getInstance().start(this.getApplicationContext());
		}

	}

	/**
	 * Payment 服务初始化
	 *
	 * @param applicationOptions
	 */
	private void initPayment(TACApplicationOptions applicationOptions) {

		TACPaymentOptions tacPaymentOptions = applicationOptions.sub("payment");
		tacPaymentOptions.setSandboxEnvironment(false);
		QCloudLogger.d("payment", "payment appid is " + tacPaymentOptions.getAppid());
	}

	/**
	 * Storage 配置初始化
	 *
	 * @param applicationOptions
	 */
	private void initStorage(TACApplicationOptions applicationOptions) {
		TACStorageOptions storageOptions = applicationOptions.sub("storage");
		// 配置签名获取服务器
		storageOptions.setCredentialProvider(new HttpRequest.Builder<String>()
				.scheme("https")
				.host("tac.cloud.tencent.com")
				.path("/client/sts")
				.method("GET")
				.query("bucket", storageOptions.getDefaultBucket())
				.build());
	}

	public boolean isMainProcess() {
		ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		String mainProcessName = getPackageName();
		int myPid = android.os.Process.myPid();
		for (ActivityManager.RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid && mainProcessName.equals(info.processName)) {
				return true;
			}
		}
		return false;
	}
}




