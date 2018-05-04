package com.example.cub05.videosamplecustom;

/**
 * Created by cub05 on 4/25/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;

/**
 * Created by BBI-M1025 on 15/05/17.
 */

public class VideoDownloadAndPlayService implements LocalFileStreamingServer.LocalFileStreamingServerCallBacks {

    private LocalFileStreamingServer server;
    VideoStreamInterface callback;

    VideoDownloadAndPlayService(LocalFileStreamingServer server) {
        this.server = server;
    }


    public void startServer(final Activity activity, String videoUrl, String pathToSaveVideo, final String ipOfServer, File file, final VideoStreamInterface callback) {

        this.callback = callback;

        SharedPreferences sharedpreferences = activity.getSharedPreferences("FilePref", Context.MODE_PRIVATE);
        int download_status = sharedpreferences.getInt("download_status", -1);
        Log.e("shared", download_status + "");


        if (file == null) {
            Log.d("sachin", "file null");
//            new VideoDownloader(activity).execute(videoUrl, pathToSaveVideo, "0");
            server = new LocalFileStreamingServer(new File(pathToSaveVideo), activity, VideoDownloadAndPlayService.this, videoUrl, pathToSaveVideo, String.valueOf(file.length()));
            server.setSupportPlayWhileDownloading(true);
        } else if (download_status == 1) {
            server = new LocalFileStreamingServer(file, activity, VideoDownloadAndPlayService.this, videoUrl, pathToSaveVideo, String.valueOf(file.length()));
            server.setSupportPlayWhileDownloading(false);
        } else {
            Log.d("sachin", "file not null");
            Log.d("sachin", "file size " + file.length());
            //new VideoDownloader(activity).execute(videoUrl, pathToSaveVideo, String.valueOf(file.length()));
            server = new LocalFileStreamingServer(file, activity, VideoDownloadAndPlayService.this, videoUrl, pathToSaveVideo, String.valueOf(file.length()));
            server.setSupportPlayWhileDownloading(true);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                server.init(ipOfServer);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        server.start();
                        callback.onServerStart(server.getFileUrl());
                    }
                });
            }
        }).start();

    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    @Override
    public void pauseVideo() {
        callback.pauseVideo();
    }

    @Override
    public void playVideo() {
        Log.e("sachin", "inPlayVideo");
        callback.playVideo();
    }

    public static interface VideoStreamInterface {
        public void onServerStart(String videoStreamUrl);

        public void pauseVideo();

        public void playVideo();


    }
}
