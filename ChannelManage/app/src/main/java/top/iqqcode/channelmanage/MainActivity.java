package top.iqqcode.channelmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mTencentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mTencentButton = findViewById(R.id.buttonTencentChannel);
        mTencentButton.setOnClickListener(MainActivity.this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonTencentChannel:
                startActivity(new Intent(MainActivity.this, ChannelActivity.class));
                break;
            default:
                break;
        }
    }
}