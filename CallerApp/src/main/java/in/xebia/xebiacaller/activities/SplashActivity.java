package in.xebia.xebiacaller.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.xebia.xebiacaller.R;

public class SplashActivity extends Activity {
    protected int _splashTime = 1500;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context = SplashActivity.this;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent();
                intent.setClass(context, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, _splashTime);
    }
}
