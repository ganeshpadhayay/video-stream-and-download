package com.example.cub05.videosamplecustom;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements VideoStreamAndDownload.ProgressBarCallbacks, NetworkChangeReceiver.NetworkChangeCallback {

    private VideoStreamAndDownload videoStreamAndDownload;

    private VideoView videoView;
    private ProgressBar progressBar;
    private MediaController mediaController;
    private Bundle dataBundle = new Bundle();
    private Button button;
    File file;

    private BroadcastReceiver mNetworkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.go_to_second_video);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        videoView = (VideoView) findViewById(R.id.videoView);
        mNetworkReceiver = new NetworkChangeReceiver(MainActivity.this);
        registerNetworkBroadcastForNougat();
        mediaController = new MediaController(this, true);
        mediaController.setAnchorView(videoView);


        File directory = getApplicationContext().getDir("xShowroom_Videos", Context.MODE_PRIVATE);
        String filePath = directory.getAbsolutePath();
        file = new File(directory, "video1.mp4");
//        try {
//            boolean fileCreated = file.createNewFile();
//            Log.e("sachin", "file created -" + fileCreated + " at location" + file.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        String videoUrl = "http://dev.xshowroom.in:8080/content/579953aca6f92bb52a5c14270eee7015/images/how_to_make_a_great_developer_5af151f9ef021.mp4";
     //   String videoUrl = "http://dev.xshowroom.in:8080/content/579953aca6f92bb52a5c14270eee7015/images/videoplayback_5afc078e03d79.mp4";
//        String videoUrl = "http://dev.xshowroom.in:8080/content/579953aca6f92bb52a5c14270eee7015/images/h1_rocks2_5afd419cb49c7.mp4";


//           String videoUrl="http://dev.xshowroom.in:8080/content/579953aca6f92bb52a5c14270eee7015/images/The%20Engineers%20talking%20tom%20new%20funny%20video_5aabf2d6cb40e.mp4";
//        String videoUrl = "http://dev.xshowroom.in:8080/content/579953aca6f92bb52a5c14270eee7015/images/2_5abcbd3e31d31.mp4";

        videoStreamAndDownload = new VideoStreamAndDownload(mediaController, videoView, MainActivity.this);
        videoStreamAndDownload.onCreate(file, file.getAbsolutePath(), videoUrl);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoStreamAndDownload.stopServer();
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }


    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
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
        synchronized (this) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    public void startProgressbar() {
        synchronized (this) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onNetworkChange(boolean internetPresent) {

        if (internetPresent) {
            //
            Log.e("sachininternet", "internet present");
            if (file != null)
                videoStreamAndDownload.startDownloading(file.length());
            else
                videoStreamAndDownload.startDownloading(0);
//            videoStreamAndDownload.onNetworkChanged(true);
        } else {
            Log.e("sachininternet", "internet absent");
//            videoStreamAndDownload.onNetworkChanged(false);
            videoStreamAndDownload.stopDownloading();
        }
    }
}
