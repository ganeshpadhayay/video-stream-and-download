package com.example.cub05.videosamplecustom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.File;

public class SecondActivity extends Activity implements VideoStreamAndDownload.ProgressBarCallbacks {


    private VideoStreamAndDownload videoStreamAndDownload;

    private VideoView videoView;
    private ProgressBar progressBar;
    private MediaController mediaController;
    private Bundle dataBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        videoView = (VideoView) findViewById(R.id.videoView);
        mediaController = new MediaController(this, true);
        mediaController.setAnchorView(videoView);


        File directory = getApplicationContext().getDir("xShowroom_Videos", Context.MODE_PRIVATE);
        String filePath = directory.getAbsolutePath();
        File file = new File(directory, "video2.mp4");

        String videoUrl = "http://192.168.100.13:8080/content/579953aca6f92bb52a5c14270eee7015/images/code_refractoring_5af1615350f5e.mp4";


        videoStreamAndDownload = new VideoStreamAndDownload(mediaController, videoView, SecondActivity.this);
        videoStreamAndDownload.onCreate(file, filePath + "/video2.mp4", videoUrl);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        dataBundle = outState;
        videoStreamAndDownload.saveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        videoStreamAndDownload.restoreInstanceState(dataBundle);
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void stopProgressbar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void startProgressbar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
}
