package com.tencent.qcloud.cosxml.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.qcloud.cosxml.sample.R;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener{

    TextView backText;
    TextView contextText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        backText = (TextView)findViewById(R.id.back);
        contextText = (TextView)findViewById(R.id.content);

        backText.setOnClickListener(this);

        Bundle bundle = this.getIntent().getExtras();
        contextText.setText(bundle.getString("RESULT"));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.back:
                finish();
                break;
        }
    }
}
