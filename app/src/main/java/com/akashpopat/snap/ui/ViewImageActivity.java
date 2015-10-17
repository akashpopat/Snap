package com.akashpopat.snap.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.akashpopat.snap.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class ViewImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        ImageView imageView = (ImageView) findViewById(R.id.pic);

        Uri imageUri = getIntent().getData();

        Picasso.with(this).load(imageUri).into(imageView);

        Picasso.with(this).load(imageUri).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 10 * 1000);
            }

            @Override
            public void onError() {
                Toast.makeText(ViewImageActivity.this,"Oops! Check network",Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}