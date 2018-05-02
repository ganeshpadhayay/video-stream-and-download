package com.example.cub05.videosamplecustom;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    String AttachmentDirName = "xShowroom_Videos";
    private VideoView videoView;
    private MediaController mediaController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("FilePref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong("fileSize", 0);
        editor.putBoolean("downloaded", false);
        editor.apply();

        videoView = (VideoView) findViewById(R.id.videoView);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        File directory = getApplicationContext().getDir(AttachmentDirName, Context.MODE_PRIVATE);
        String filePath = directory.getAbsolutePath();
        File file = new File(directory, "video1.mp4");

        if (file.exists()) {
            startServer(filePath, file);
        } else {
            startServer(filePath, null);
        }

    }

    private VideoDownloadAndPlayService videoService;

    private void startServer(String filePath, final File file) {
        videoService = VideoDownloadAndPlayService.startServer(MainActivity.this,
                "http://192.168.100.13:8080/content/579953aca6f92bb52a5c14270eee7015/images/Triumph Bonneville T100 - Road Test Review - ZigWheels_5a82825d00d03.mp4", filePath + "/video1.mp4", "127.0.0.1", file, new VideoDownloadAndPlayService.VideoStreamInterface() {
                    @Override
                    public void onServerStart(String videoStreamUrl) {
                        // use videoStreamUrl to play video through media player
                        Log.d("sachin", videoStreamUrl);

                        videoView.setMediaController(mediaController);
                        videoView.setKeepScreenOn(true);
                        videoView.setVideoPath(videoStreamUrl);
                        videoView.start();
                        videoView.requestFocus();
//                Log.d("sachin", file.toString());
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (videoService != null)
//            videoService.stop();
    }


}
