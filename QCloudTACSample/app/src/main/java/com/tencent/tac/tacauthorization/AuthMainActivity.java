package com.tencent.tac.tacauthorization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.core.auth.OAuth2Credentials;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.common.QCloudResultListener;
import com.tencent.qcloud.core.common.QCloudServiceException;
import com.tencent.tac.R;
import com.tencent.tac.authorization.TACAuthorizationService;
import com.tencent.tac.authorization.TACOpenUserInfo;
import com.tencent.tac.authorization.oauth2.QQAuthProvider;
import com.tencent.tac.authorization.oauth2.WeChatAuthProvider;

import java.text.DateFormat;

public class AuthMainActivity extends Activity implements QCloudResultListener<OAuth2Credentials> {

    TextView currentUserView;
    TextView userInfoView;

    QQAuthProvider qqAuthProvider;
    WeChatAuthProvider weChatAuthProvider;
    OAuth2Credentials mLastCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_main);

        currentUserView = (TextView) findViewById(R.id.current_user);
        userInfoView = (TextView) findViewById(R.id.user_info);

        TACAuthorizationService service = TACAuthorizationService.getInstance();
        qqAuthProvider = service.getQQAuthProvider(this);
        weChatAuthProvider = service.getWeChatAuthProvider(this);
    }

    public void loginQQ(View view) {
        qqAuthProvider.signIn(this, this);
        ((TextView) findViewById(R.id.platform)).setText("平台：QQ");
    }

    public void loginWeChat(View view) {
        weChatAuthProvider.signIn(this);
        ((TextView) findViewById(R.id.platform)).setText("平台：微信");
    }

    public void RefreshWeChatToken(View view) {
        if (mLastCredentials != null && mLastCredentials.isExpired()) {
            if (WeChatAuthProvider.PLATFORM.equals(mLastCredentials.getPlatform())) {
                // 后台刷新微信token
                if (mLastCredentials.getRefreshToken() != null) {
                    weChatAuthProvider.refreshCredentialInBackground(mLastCredentials,
                            new QCloudResultListener<OAuth2Credentials>() {
                                @Override
                                public void onSuccess(OAuth2Credentials result) {
                                    Toast.makeText(AuthMainActivity.this, "token刷新成功", Toast.LENGTH_LONG).show();
                                    AuthMainActivity.this.onSuccess(result);
                                }

                                @Override
                                public void onFailure(QCloudClientException clientException,
                                                      QCloudServiceException serviceException) {
                                    if (WeChatAuthProvider.isUserNeedSignIn(serviceException)) {
                                        // 刷新失败，需要用户重新登录微信授权
                                        loginWeChat(null);
                                    } else {
                                        Toast.makeText(AuthMainActivity.this, "token刷新失败",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            } else if (QQAuthProvider.PLATFORM.equals(mLastCredentials.getPlatform())) {
                // 用户重新登录QQ授权
                loginQQ(null);
            } else {
                Toast.makeText(AuthMainActivity.this, "无法刷新token",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(AuthMainActivity.this, "无token 或者 token有效",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void GetUserInfo(View view) {
        QCloudResultListener<TACOpenUserInfo> resultListener = new QCloudResultListener<TACOpenUserInfo>() {
            @Override
            public void onSuccess(TACOpenUserInfo result) {
                Log.d("authorization", result.toString());
                userInfoView.setText(result.toString());
            }

            @Override
            public void onFailure(QCloudClientException clientException, QCloudServiceException serviceException) {
                Toast.makeText(AuthMainActivity.this, "获取用户信息出错", Toast.LENGTH_LONG).show();
            }
        };
        if (mLastCredentials != null) {
            if (!mLastCredentials.isExpired()) {
                userInfoView.setText("");
                if (WeChatAuthProvider.PLATFORM.equals(mLastCredentials.getPlatform())) {
                    weChatAuthProvider.getUserInfo(mLastCredentials, resultListener);
                } else if (QQAuthProvider.PLATFORM.equals(mLastCredentials.getPlatform())) {
                    qqAuthProvider.getUserInfo(mLastCredentials, resultListener);
                }
            } else if (mLastCredentials.getOpenId() != null) {
                Toast.makeText(AuthMainActivity.this, "token过期，需要刷新或者重新登录",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AuthMainActivity.this, "没有获取用户id",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (qqAuthProvider != null) {
            qqAuthProvider.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSuccess(OAuth2Credentials result) {
        mLastCredentials = result;
        currentUserView.setText("open id : " + result.getOpenId() + "\n" +
                "access token : " + result.getAccessToken() + "\n" +
                "authorization code : " + result.getAuthorizationCode() + "\n" +
                "expires in : " + DateFormat.getInstance().format(result.getValidFromDate()));
        userInfoView.setText("");
    }

    @Override
    public void onFailure(QCloudClientException clientException, QCloudServiceException serviceException) {
        Toast.makeText(AuthMainActivity.this, "登陆出错", Toast.LENGTH_LONG).show();
    }
}
