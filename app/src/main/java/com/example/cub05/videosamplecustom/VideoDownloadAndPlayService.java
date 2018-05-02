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

public class VideoDownloadAndPlayService {

    private static LocalFileStreamingServer server;

    private VideoDownloadAndPlayService(LocalFileStreamingServer server) {
        this.server = server;
    }

    public static VideoDownloadAndPlayService startServer(final Activity activity, String videoUrl, String pathToSaveVideo, final String ipOfServer, File file, final VideoStreamInterface callback) {

        SharedPreferences sharedpreferences = activity.getSharedPreferences("FilePref", Context.MODE_PRIVATE);
        boolean downloaded = sharedpreferences.getBoolean("downloaded", false);
        Log.e("shared", sharedpreferences.getBoolean("downloaded", false) + "");


        if (file == null) {
            Log.d("sachin", "file null");
            new VideoDownloader(activity).execute(videoUrl, pathToSaveVideo, "0");
            server = new LocalFileStreamingServer(new File(pathToSaveVideo), activity);
            server.setSupportPlayWhileDownloading(true);
        } else if (downloaded) {
            server = new LocalFileStreamingServer(file, activity);
            server.setSupportPlayWhileDownloading(false);
        } else {
            Log.d("sachin", "file not null");
            Log.d("sachin", "file size " + file.length());
            new VideoDownloader(activity).execute(videoUrl, pathToSaveVideo, String.valueOf(file.length()));
            server = new LocalFileStreamingServer(file, activity);
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

        return new VideoDownloadAndPlayService(server);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public static interface VideoStreamInterface {
        public void onServerStart(String videoStreamUrl);
    }
}
