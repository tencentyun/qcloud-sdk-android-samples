package com.tencent.qcloud.costransferpractice.transfer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.qcloud.costransferpractice.COSConfigManager;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.FilePathHelper;
import com.tencent.qcloud.costransferpractice.common.LoadingDialogFragment;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by rickenwang on 2018/10/18.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class TransferFragment extends Fragment implements TransferContract.View, View.OnClickListener{

    private final int OPEN_FILE_CODE = 10002;

    TransferContract.Presenter transferPresenter;

    View contentView;

    private ProgressBar uploadProgress;
    private ProgressBar downloadProgress;

    private TextView uploadState;
    private TextView downloadState;

    private TextView uploadProgressText;
    private TextView downloadProgressText;

    private Spinner regionSpinner;
    private Spinner bucketSpinner;

    private LoadingDialogFragment loadingDialog;

    private ImageView chooseFilePreImage;
    private TextView chooseFileName;

    AnimationDrawable animationDrawable;


    Map<String, List<String>> regionAndBuckets;

    final private String REGION_POSITION_KEY = "region_position_key";
    final private String BUCKET_POSITION_KEY = "bucket_position_key";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_transfer, container, false);
        initContentView(contentView);
        transferPresenter.start();

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setPresenter(TransferContract.Presenter presenter) {

        this.transferPresenter = presenter;
    }

    private void initContentView(View contentView) {

        uploadProgress = contentView.findViewById(R.id.upload_progress);
        downloadProgress = contentView.findViewById(R.id.download_progress);
        uploadState = contentView.findViewById(R.id.upload_state);
        downloadState = contentView.findViewById(R.id.download_state);
        uploadProgressText = contentView.findViewById(R.id.upload_progress_text);
        downloadProgressText = contentView.findViewById(R.id.download_progress_text);

        regionSpinner = contentView.findViewById(R.id.region_spinner);
        bucketSpinner = contentView.findViewById(R.id.bucket_spinner);

        Button startUpload = contentView.findViewById(R.id.upload_start);
        Button pauseUpload = contentView.findViewById(R.id.upload_pause);
        Button resumeUpload = contentView.findViewById(R.id.upload_resume);
        Button cancelUpload = contentView.findViewById(R.id.upload_cancel);

        Button startDownload = contentView.findViewById(R.id.download_start);
        Button pauseDownload = contentView.findViewById(R.id.download_pause);
        Button resumeDownload = contentView.findViewById(R.id.download_resume);
        Button cancelDownload = contentView.findViewById(R.id.download_cancel);

        Button chooseFile = contentView.findViewById(R.id.choose_file);

        startUpload.setOnClickListener(this);
        pauseUpload.setOnClickListener(this);
        resumeUpload.setOnClickListener(this);
        cancelUpload.setOnClickListener(this);

        startDownload.setOnClickListener(this);
        pauseDownload.setOnClickListener(this);
        resumeDownload.setOnClickListener(this);
        cancelDownload.setOnClickListener(this);

        chooseFile.setOnClickListener(this);

        ImageView piano = contentView.findViewById(R.id.piano);
        animationDrawable = (AnimationDrawable) piano.getBackground();

        loadingDialog = new LoadingDialogFragment();

        chooseFileName = contentView.findViewById(R.id.choose_file_name);
        chooseFilePreImage = contentView.findViewById(R.id.choose_file_image);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.upload_start:
                transferPresenter.startUpload();
                break;
            case R.id.download_start:
                transferPresenter.startDownload();
                break;

            case R.id.upload_pause:
                transferPresenter.pauseUpload();
                break;
            case R.id.download_pause:
                transferPresenter.pauseDownload();
                break;

            case R.id.upload_resume:
                transferPresenter.resumeUpload();
                break;
            case R.id.download_resume:
                transferPresenter.resumeDownload();
                break;

            case R.id.upload_cancel:
                transferPresenter.cancelUpload();
                break;
            case R.id.download_cancel:
                transferPresenter.cancelDownload();
                break;

            case R.id.choose_file:
                openFileSelector();
                break;
        }
    }

    @Override
    public void toastMessage(final String message) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void refreshUploadState(final TransferState state) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                uploadState.setText(state.toString());
            }
        });

    }

    @Override
    public void refreshDownloadState(final TransferState state) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                downloadState.setText(state.toString());
            }
        });
    }

    @Override
    public void refreshUploadProgress(final long progress, final long total) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                uploadProgress.setProgress((int) (100 * progress / total));
                uploadProgressText.setText(size(progress) + "/" + size(total));

                if (atLastFrame() || !animationDrawable.isRunning()) {
                    animationDrawable.setVisible(false, true);
                    animationDrawable.start();
                }
            }
        });
    }

    @Override
    public void refreshDownloadProgress(final long progress, final long total) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                downloadProgress.setProgress((int) (100 * progress / total));
                downloadProgressText.setText(size(progress) + "/" + size(total));

                if (atLastFrame() || !animationDrawable.isRunning()) {
                    animationDrawable.setVisible(false, true);
                    animationDrawable.start();
                }
            }
        });
    }

    private boolean atLastFrame() {

        int lastFrame = animationDrawable.getNumberOfFrames();

        return animationDrawable.getCurrent() == animationDrawable.getFrame(lastFrame-1);
    }

    @Override
    public void setLoading(boolean loading) {

        if (loading) {

            loadingDialog.show(getActivity().getFragmentManager(), "loading");
        } else {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void clearTransferProgressAndState() {

        uploadProgress.setProgress(0);
        uploadProgressText.setText("");
        uploadState.setText("无");

        downloadProgress.setProgress(0);
        downloadProgressText.setText("");
        downloadState.setText("无");
    }

    @Override
    public void showRegionAndBucket(final Map<String, List<String>> buckets) {

        regionAndBuckets = buckets;

        final List<String> regions = new LinkedList<>(buckets.keySet());

        regionSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, R.id.item,
                regions));

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                refreshBucketSpinner(position, 0);
                transferPresenter.refreshRegion(regions.get(position), position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bucketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                transferPresenter.refreshBucket((String) bucketSpinner.getAdapter().getItem(position),
                        position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void restore(int regionPosition, int bucketPosition) {

        refreshRegionSpinner(regionPosition);
        refreshBucketSpinner(regionPosition, bucketPosition);
    }

    @Override
    public void refreshChooseFile(String fileName) {

        if (TextUtils.isEmpty(fileName)) {

            chooseFilePreImage.setVisibility(View.GONE);
            chooseFileName.setText("");
        } else {

            chooseFilePreImage.setVisibility(View.VISIBLE);
            chooseFileName.setText(fileName);
        }
    }

    private void refreshRegionSpinner(int regionPosition) {

        if (regionPosition > 0) {
            regionSpinner.setSelection(regionPosition);
        }

        final List<String> regions = new LinkedList<>(regionAndBuckets.keySet());
    }

    /**
     *
     * @param regionPosition
     * @param bucketPosition
     */
    private void refreshBucketSpinner(int regionPosition, int bucketPosition) {

        String region = new LinkedList<>(regionAndBuckets.keySet()).get(regionPosition);
        List<String> buckets = regionAndBuckets.get(region);

        bucketSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, R.id.item,
                buckets));

        if (bucketPosition > 0) {
            bucketSpinner.setSelection(bucketPosition);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        transferPresenter.release();
    }

    private String size(long size) {

        float realSize = size;
        int index = 0;
        String [] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};

        while (realSize > 1000 && index < 5) {

            index++;
            realSize /= 1024;
        }

        String capacityText =  new DecimalFormat("###,###,###.##").format(realSize);
        return String.format(Locale.ENGLISH, "%s%s", capacityText, units[index]);
    }

    void openFileSelector() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_FILE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK) {

            String path = FilePathHelper.getPath(getActivity(), data.getData());
            refreshChooseFile(path);
            transferPresenter.refreshUploadCosAndLocalPath(path);
            clearTransferProgressAndState();
        }
    }
}
