package com.tencent.qcloud.costransferpractice.bucket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.model.tag.ListAllMyBuckets;
import com.tencent.qcloud.costransferpractice.BuildConfig;
import com.tencent.qcloud.costransferpractice.CosServiceFactory;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.base.BaseActivity;
import com.tencent.qcloud.costransferpractice.object.ObjectActivity;

import java.util.List;

/**
 * Created by jordanqin on 2020/6/18.
 * 存储桶列表页
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class BucketActivity extends BaseActivity {
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;
    private final int REQUEST_ADD = 10003;

    private CosXmlService cosXmlService;

    private ListView listview;
    private BucketsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_list_activity);

        requestPermissions();

        listview = findViewById(R.id.listview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAllMyBuckets.Bucket bucket = adapter.getItem(position);
                if(bucket!=null){
                    Intent intent = new Intent(BucketActivity.this, ObjectActivity.class);
                    intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_BUCKET_NAME, bucket.name);
                    intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_REGION, bucket.location);
                    startActivity(intent);
                }
            }
        });

        if (TextUtils.isEmpty(BuildConfig.COS_SECRET_ID) || TextUtils.isEmpty(BuildConfig.COS_SECRET_KEY)) {
            toastMessage("请在环境变量中配置您的secretId和secretKey");
        } else {
            cosXmlService = CosServiceFactory.getCosXmlServiceByGetService(this, BuildConfig.COS_SECRET_ID, BuildConfig.COS_SECRET_KEY, false);
            getBuckets();
        }
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled(){
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bucket, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            if (TextUtils.isEmpty(BuildConfig.COS_SECRET_ID) ||
                    TextUtils.isEmpty(BuildConfig.COS_SECRET_KEY) ||
                    TextUtils.isEmpty(BuildConfig.COS_APP_ID)) {
                toastMessage("请在环境变量中配置您的secretId、secretKey、appid");
            } else {
                startActivityForResult(new Intent(this, BucketAddActivity.class), REQUEST_ADD);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_ADD){
            getBuckets();
        }
    }

    private void getBuckets(){
        setLoading(true);
        cosXmlService.getServiceAsync(new GetServiceRequest(), new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request,final CosXmlResult result) {
                uiAction(new Runnable() {
                    @Override
                    public void run() {
                        setLoading(false);
                        List<ListAllMyBuckets.Bucket> buckets = ((GetServiceResult) result).listAllMyBuckets.buckets;
//                        // 通过bucket名称过滤出对应buckets列表
//                        List<ListAllMyBuckets.Bucket> filteredBuckets = buckets.stream()
//                                .filter(bucket -> bucket.name.equals("mobile-ut-1253960454"))
//                                .collect(java.util.stream.Collectors.toList());
//
//                        if(adapter==null){
//                            adapter = new BucketsAdapter(filteredBuckets, BucketActivity.this);
//                            listview.setAdapter(adapter);
//                        } else {
//                            adapter.setDataList(filteredBuckets);
//                        }

                        if(adapter==null){
                            adapter = new BucketsAdapter(buckets, BucketActivity.this);
                            listview.setAdapter(adapter);
                        } else {
                            adapter.setDataList(buckets);
                        }
                    }
                });
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                setLoading(false);
                toastMessage("获取存储桶列表失败");
                if(exception!=null) {
                    exception.printStackTrace();
                }
                if(serviceException!=null) {
                    serviceException.printStackTrace();
                }
            }
        });
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        }
    }
}
