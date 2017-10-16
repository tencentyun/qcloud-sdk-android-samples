package com.tencent.qcloud.facein.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.qcloud.facein.activity.FaceInEntryActivity;
import com.tencent.qcloud.facein.user.FaceInConfig;
import com.tencent.qcloud.facein.user.FaceInFailedType;
import com.tencent.qcloud.facein.user.FaceInModelType;
import com.tencent.qcloud.facein.user.FaceInResultListener;
import com.tencent.qcloud.facein.user.IdCardInfo;
import com.tencent.qcloud.network.cosv4.CosV4CredentialProvider;
import com.tencent.qcloud.network.exception.QCloudException;
import com.tencent.qcloud.network.logger.QCloudLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends AppCompatActivity {

    Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_in_demo);

        // 一、填写您在腾讯云上的基本信息

        // 1、appid
        String appid = "1000001";
        // 2、secret id，secret id不同于secret key，可以将其放在客户端
        String secret_id = "AKIDmO5cPW3LDGJsarDEDcQ1FprZVC1opgqX";
        // 3、该appid下的bucket名称
        String bucket = "qiniutest2";

        FaceInConfig.setAppid(appid);
        FaceInConfig.setBucket(bucket);

        // 二、配置签名类，注意这里CosV4CredentialProvider的实现方式给一个固定的签名串，正式环境下请自行搭建服务生成并由客户端获取
        CosV4CredentialProvider credentialProvider = new CosV4CredentialProvider(appid, secret_id) {
            @Override
            public String encrypt(String source) throws QCloudException {
                return "urk39XBQpk8fHEJqpfEf3FzHgpBhPTEwMDAwMDEmaz1BS0lEbU81Y1BXM0xER0pzYXJERURjUTFGcHJaVkMxb3BncVgmZT0xNTA4Mzc4NTcyJnQ9MTUwNTc4NjU3MiZyPTQxOTAyNDYwOTQmZj0vMTAwMDAwMS9xaW5pdXRlc3QyLyZiPXFpbml1dGVzdDI=";

            }
        };
        FaceInConfig.setCredentialProvider(credentialProvider);

        // 三、其他配置（可选）

        // 1、超时时间设置
        FaceInConfig.setHttpConnectTimeout(5000);
        FaceInConfig.setHttpSocketTimeout(20000);

        // 2、设置识别阈值
        FaceInConfig.setIdCardPersonCompareConfidence(70);
        FaceInConfig.setLipVideoPersonCompareConfidence(70);
        FaceInConfig.setFaceAliveCompareConfidence(70);

        // 3、是否打印调试信息
        FaceInConfig.setDebug(true);

        // 四、设置识别回调
        FaceInConfig.setFaceInResultListener(new FaceInResultListener() {
            @Override
            public void onSuccess(FaceInModelType modelType, IdCardInfo person) {

                QCloudLogger.debug(logger, "{} 识别成功，识别人信息为：{}", modelType.getMessage(), person.toString());
            }

            @Override
            public void onFailed(FaceInModelType modelType, FaceInFailedType failedType) {

                QCloudLogger.debug(logger, "{} 识别失败，失败原因为：{}", modelType.getMessage(), failedType.getMessage());
            }
        });


        Button baseOcr = (Button) findViewById(R.id.start_base_ocr_activity);
        baseOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceInConfig.setOcrModel(FaceInModelType.OCR_ID_CARD_IMAGE_COMPARE);
                Intent intent = new Intent(MainActivity.this, FaceInEntryActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button lipOcr = (Button) findViewById(R.id.start_lip_ocr_activity);
        lipOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceInConfig.setOcrModel(FaceInModelType.OCR_ID_CARD_LIP_VIDEO_COMPARE);
                Intent intent = new Intent(MainActivity.this, FaceInEntryActivity.class);
                MainActivity.this.startActivity(intent);

            }
        });

        Button faceAlive = (Button) findViewById(R.id.start_face_ocr_activity);
        faceAlive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceInConfig.setOcrModel(FaceInModelType.OCR_ID_CARD_FACE_ALIVE_COMPARE);
                Intent intent = new Intent(MainActivity.this, FaceInEntryActivity.class);
                MainActivity.this.startActivity(intent);

            }
        });

        CrashReport.initCrashReport(getApplicationContext(), "f214ace69c", true);

    }
}
