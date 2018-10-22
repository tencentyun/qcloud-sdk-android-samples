package com.tencent.qcloud.costransferpractice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tencent.qcloud.costransferpractice.login.LoginFragment;
import com.tencent.qcloud.costransferpractice.transfer.TransferFragment;
import com.tencent.qcloud.costransferpractice.transfer.TransferPresenter;

import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;

    private Fragment loginFragment;
    private TransferFragment transferFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        loginFragment();
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

    private void loginFragment() {

        loginFragment = new LoginFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, loginFragment);
        fragmentTransaction.commit();
    }

    public void transferFragment(Map<String, List<String>> regionAndBuckets) {

        transferFragment = new TransferFragment();
        TransferPresenter transferPresenter = new TransferPresenter(this, transferFragment, regionAndBuckets);
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(loginFragment);
        fragmentTransaction.add(R.id.fragment_container, transferFragment);
        fragmentTransaction.commit();
    }
}
