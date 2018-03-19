package com.tencent.tac.tacanalytics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tencent.tac.R;
import com.tencent.tac.analytics.TACAnalyticsService;
import com.tencent.tac.analytics.TACAnalyticsEvent;
import com.tencent.tac.analytics.TACNetworkMetrics;

import java.util.Properties;

public class AnalyticsMainActivity extends Activity {

	private static final String TAG = "TACAnalyticsDemo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.analytics_activity_first);
	}

	public void trackEvent(View v) {
		TACAnalyticsService.getInstance().trackEvent(this, new TACAnalyticsEvent("oneEvent"));
	}

	public void trackEventWithArgs(View v) {
		Properties properties = new Properties();
		properties.put("twoKey", "twoValue");
		TACAnalyticsService.getInstance().trackEvent(this, new TACAnalyticsEvent("twoEvent", properties));
	}

	public void trackDurationEvent(View v) {
		TACAnalyticsService.getInstance().trackEventDuration(this, new TACAnalyticsEvent("oneDurationEvent"), 2000);
	}

	public void trackDurationEventBegin(View v) {
		Properties properties = new Properties();
		properties.put("twoKeyDuration", "twoValueDuration");
		TACAnalyticsService.getInstance().trackEventDurationBegin(this, new TACAnalyticsEvent("twoDurationEvent", properties));
	}

	public void trackDurationEventEnd(View v) {
		Properties properties = new Properties();
		properties.put("twoKeyDuration", "twoValueDuration");
		TACAnalyticsService.getInstance().trackEventDurationEnd(this, new TACAnalyticsEvent("twoDurationEvent", properties));
	}

	public void newSession(View v) {
		TACAnalyticsService.getInstance().exchangeNewSession(this);
	}

	public void trackPage(View v) {
		startActivity(new Intent(this, SecondActivity.class));
	}

//	public void trackNetworkMetrics(View v) {
//		// 新建监控接口对象
//		TACNetworkMetrics monitor = new TACNetworkMetrics("ping:www.qq.com");
//		// 接口开始执行
//		String ip = "www.qq.com";
//		Runtime run = Runtime.getRuntime();
//		Process proc = null;
//		try {
//			String str = "ping -c 3 -i 0.2 -W 1 " + ip;
//			long starttime = System.currentTimeMillis();
//			proc = run.exec(str);
//			int retCode = proc.waitFor();
//			long difftime = System.currentTimeMillis() - starttime;
//
//			monitor
//			// 设置接口耗时
//			.setMillisecondsConsume(difftime)
//			// 设置接口返回码
//			.setReturnCode(retCode)
//			// 设置请求包大小，若有的话
//			.setReqSize(1000)
//			// 设置响应包大小，若有的话
//			.setRespSize(2000);
//			// 设置抽样率，默认为1，表示100%。如果是50%，则填2(100/50)，如果是25%，则填4(100/25)，以此类推。
//			// .setSampling(2)
//
//			if (retCode == 0) {
//				Log.d(TAG, "ping连接成功");
//				// 标记为成功
//				monitor.setResultType(TACNetworkMetrics.SUCCESS_RESULT_TYPE);
//			} else {
//				Log.d(TAG, "ping测试失败");
//				// 标记为逻辑失败，可能由网络未连接等原因引起的，但对于业务来说不是致命的，是可容忍的
//				monitor.setResultType(TACNetworkMetrics.LOGIC_FAILURE_RESULT_TYPE);
//			}
//		} catch (Exception e) {
//			Log.d(TAG, e.toString());
//			// 接口调用出现异常，致命的，标识为失败
//			monitor.setResultType(TACNetworkMetrics.FAILURE_RESULT_TYPE);
//		} finally {
//			proc.destroy();
//		}
//		// 上报接口监控的信息
//		TACAnalyticsService.getInstance().trackNetworkMetrics(this, monitor);
//	}
//
//	public void setUserProperties(View v) {
//		Properties properties = new Properties();
//		properties.put("name", "abc");
//		TACAnalyticsService.getInstance().setUserProperties(this, properties);
//	}
//
//	public void getProperty(View v) {
//		Log.d(TAG, "getProperty() = " + TACAnalyticsService.getInstance().getCustomProperty(this, "sex"));
//	}
}
