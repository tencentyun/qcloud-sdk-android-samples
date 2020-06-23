package com.tencent.qcloud.costransferpractice.object;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.bucket.GetBucketRequest;
import com.tencent.cos.xml.model.bucket.GetBucketResult;
import com.tencent.cos.xml.model.object.DeleteObjectRequest;
import com.tencent.qcloud.costransferpractice.BuildConfig;
import com.tencent.qcloud.costransferpractice.CosServiceFactory;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.base.BaseActivity;
import com.tencent.qcloud.costransferpractice.transfer.DownloadActivity;
import com.tencent.qcloud.costransferpractice.transfer.UploadActivity;

/**
 * Created by jordanqin on 2020/6/18.
 * 对象列表页
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class ObjectActivity extends BaseActivity implements AbsListView.OnScrollListener, ObjectAdapter.OnObjectListener {
    public final static String ACTIVITY_EXTRA_BUCKET_NAME = "bucket_name";
    public final static String ACTIVITY_EXTRA_FOLDER_NAME = "folder_name";
    public final static String ACTIVITY_EXTRA_REGION = "bucket_region";
    public final static String ACTIVITY_EXTRA_DOWNLOAD_KEY = "download_key";

    private final int REQUEST_UPLOAD = 10001;

    private CosXmlService cosXmlService;

    private ListView listview;
    private ObjectAdapter adapter;
    private TextView footerView;

    private String bucketName;
    private String folderName;
    private String bucketRegion;

    //是否到底部
    private boolean isBottom;
    //分页标示
    private String marker;
    //是否截断（用来判断分页数据是否完全加载）
    private boolean isTruncated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_list_activity);

        bucketName = getIntent().getStringExtra(ACTIVITY_EXTRA_BUCKET_NAME);
        folderName = getIntent().getStringExtra(ACTIVITY_EXTRA_FOLDER_NAME);
        bucketRegion = getIntent().getStringExtra(ACTIVITY_EXTRA_REGION);

        if (getSupportActionBar() != null) {
            if (TextUtils.isEmpty(folderName)) {
                getSupportActionBar().setTitle(bucketName);
            } else {
                getSupportActionBar().setTitle(folderName);
            }
        }

        listview = findViewById(R.id.listview);
        listview.setOnScrollListener(this);
        footerView = new TextView(this);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        footerView.setPadding(0, 30, 0, 30);
        footerView.setLayoutParams(params);
        footerView.setGravity(Gravity.CENTER);
        footerView.setTextColor(Color.parseColor("#666666"));
        footerView.setTextSize(16);
        listview.setFooterDividersEnabled(false);
        listview.addFooterView(footerView);

        if (TextUtils.isEmpty(BuildConfig.COS_SECRET_ID) || TextUtils.isEmpty(BuildConfig.COS_SECRET_KEY) ||
                TextUtils.isEmpty(bucketRegion)) {
            finish();
        } else {
            cosXmlService = CosServiceFactory.getCosXmlService(this, bucketRegion, BuildConfig.COS_SECRET_ID, BuildConfig.COS_SECRET_KEY, true);
            getObject();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.object, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            Intent intent = new Intent(this, UploadActivity.class);
            intent.putExtra(ACTIVITY_EXTRA_REGION, bucketRegion);
            intent.putExtra(ACTIVITY_EXTRA_BUCKET_NAME, bucketName);
            intent.putExtra(ACTIVITY_EXTRA_FOLDER_NAME, folderName);
            startActivityForResult(intent, REQUEST_UPLOAD);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_UPLOAD) {
            // TODO: 2020/6/19 添加实体还是整体刷新 查看客户端实现方式
            //临时全量刷新
            marker=null;
            getObject();
        }
    }

    private void getObject() {
        String bucketName = this.bucketName;
        final GetBucketRequest getBucketRequest = new GetBucketRequest(bucketName);

        // 前缀匹配，用来规定返回的对象前缀地址
        if (!TextUtils.isEmpty(folderName)) {
            getBucketRequest.setPrefix(folderName);
        }

        // 如果是第一次调用，您无需设置 marker 参数，COS 会从头开始列出对象
        // 如果需列出下一页对象，则需要将 marker 设置为上次列出对象时返回的 GetBucketResult.listBucket.nextMarker 值
        // 如果返回的 GetBucketResult.listBucket.isTruncated 为 false，则说明您已经列出了所有满足条件的对象
        if (!TextUtils.isEmpty(marker)) {
            getBucketRequest.setMarker(marker);
        }

        // 单次返回最大的条目数量，默认1000
        getBucketRequest.setMaxKeys(100);

        // 定界符为一个符号，如果有 Prefix，
        // 则将 Prefix 到 delimiter 之间的相同路径归为一类，定义为 Common Prefix，
        // 然后列出所有 Common Prefix。如果没有 Prefix，则从路径起点开始
        getBucketRequest.setDelimiter("/");

        //首页加载弹窗loading  非首页底部loading
        if (TextUtils.isEmpty(marker)) {
            setLoading(true);
        } else {
            footerView.setText("正在加载数据...");
        }

        // 使用异步回调请求
        cosXmlService.getBucketAsync(getBucketRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                final GetBucketResult getBucketResult = (GetBucketResult) result;
                isTruncated = getBucketResult.listBucket.isTruncated;

                uiAction(new Runnable() {
                    @Override
                    public void run() {
                        //首页加载弹窗loading  非首页底部loading
                        if (TextUtils.isEmpty(marker)) {
                            setLoading(false);
                        }
                        if (!isTruncated) {
                            footerView.setText("无更多数据");
                        }

                        marker = getBucketResult.listBucket.nextMarker;
                        if (adapter == null) {
                            adapter = new ObjectAdapter(ObjectEntity.listBucket2ObjectList(getBucketResult.listBucket, folderName),
                                    ObjectActivity.this, ObjectActivity.this, folderName);
                            listview.setAdapter(adapter);
                        } else {
                            //首页加载弹窗loading  非首页底部loading
                            if (TextUtils.isEmpty(marker)) {
                                adapter.setDataList(ObjectEntity.listBucket2ObjectList(getBucketResult.listBucket, folderName));
                            } else {
                                adapter.addDataList(ObjectEntity.listBucket2ObjectList(getBucketResult.listBucket, folderName));
                            }
                        }
                    }
                });
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {
                //首页加载弹窗loading  非首页底部loading
                if (TextUtils.isEmpty(marker)) {
                    setLoading(false);
                    toastMessage("获取对象列表失败");
                } else {
                    footerView.setText("获取对象列表失败");
                }

                clientException.printStackTrace();
                serviceException.printStackTrace();
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //滑动停止后开始请求数据
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (isBottom && isTruncated && !TextUtils.isEmpty(marker)) {
                getObject();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //是否滑动到底部
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            isBottom = true;
        } else {
            isBottom = false;
        }
    }

    @Override
    public void onFolderClick(String prefix) {
        Intent intent = new Intent(this, ObjectActivity.class);
        intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_BUCKET_NAME, bucketName);
        intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_REGION, bucketRegion);
        intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_FOLDER_NAME, prefix);
        startActivity(intent);
    }

    @Override
    public void onDownload(final ObjectEntity object) {
        Intent intent = new Intent(this, DownloadActivity.class);
        intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_BUCKET_NAME, bucketName);
        intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_REGION, bucketRegion);
        intent.putExtra(ObjectActivity.ACTIVITY_EXTRA_DOWNLOAD_KEY, object.getContents().key);
        startActivity(intent);
    }

    @Override
    public void onDelete(final ObjectEntity object) {
        String bucket = this.bucketName;

        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, object.getContents().key);

        setLoading(true);
        cosXmlService.deleteObjectAsync(deleteObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult result) {
                uiAction(new Runnable() {
                    @Override
                    public void run() {
                        setLoading(false);
                        adapter.delete(object);
                    }
                });
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {
                setLoading(false);
                toastMessage("删除对象失败");
                clientException.printStackTrace();
                serviceException.printStackTrace();
            }
        });
    }
}
