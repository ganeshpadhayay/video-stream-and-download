package com.example.cub05.videosamplecustom;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by cub05 on 5/3/2018.
 */

public class VideoStreamAndDownload implements LocalFileStreamingServer.LocalFileStreamingServerCallBacks {


    private VideoView videoView;
    private MediaController mediaController;
    private int stopPosition;

    private LocalFileStreamingServer server;


    private MainActivity activity;


    public VideoStreamAndDownload(MediaController mediaController, VideoView videoView, MainActivity mainActivity) {
        this.mediaController = mediaController;
        this.videoView = videoView;
        this.activity = mainActivity;
    }

    public void onCreate(File file, String filePath, String videoUrl) {

        if (file.exists()) {
            startServer(activity, videoUrl, filePath + "/video1.mp4", "127.0.0.1", file);
        } else {
            startServer(activity, videoUrl, filePath + "/video1.mp4", "127.0.0.1", null);
        }


    }
//
//    private void startServer(final String filePath, final File file) {
//        videoService.startServer(MainActivity.this,
//                "http://192.168.100.13:8080/content/579953aca6f92bb52a5c14270eee7015/images/Triumph Bonneville T100 - Road Test Review - ZigWheels_5a82825d00d03.mp4", filePath + "/video1.mp4", "127.0.0.1", file, new VideoDownloadAndPlayService.VideoStreamInterface() {
//                    @Override
//                    public void onServerStart(String videoStreamUrl) {
//                        // use videoStreamUrl to play video through media player
////                        Log.d("sachin", videoStreamUrl);
////
////                        videoView.setMediaController(mediaController);
////                        videoView.setKeepScreenOn(true);
////                        videoView.setVideoPath(videoStreamUrl);
////                        videoView.start();
////                        videoView.requestFocus();
////
////                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
////                            @Override
////                            public void onPrepared(MediaPlayer mp) {
////                                SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("FilePref", Context.MODE_PRIVATE);
////                                long fileLength = sharedpreferences.getLong("file_length", -1);
////                                int fileDuration = videoView.getDuration();
////
////                                long fileSizePerSec = fileLength / (fileDuration / 1000);
////                                Log.e("test", "fileLenth: " + fileLength + " fileDuration : " + fileDuration + " fileSizePerSec : " + fileSizePerSec);
////                            }
////                        });
//
//
//                    }
//
//                    @Override
//                    public void pauseVideo() {
//                        Log.e("sachin", "paused");
//                        videoView.pause();
//                        stopPosition = videoView.getCurrentPosition();
//                    }
//
//                    @Override
//                    public void playVideo() {
//                        Log.e("sachin", "resumed");
//                        videoView.seekTo(stopPosition);
//                        videoView.start();
//                    }
//                });
//    }

    public void startServer(final Activity activity, String videoUrl, String pathToSaveVideo, final String ipOfServer, File file) {


        if (file == null) {
            Log.d("sachin", "file null");
            //  new VideoDownloader(activity).execute(videoUrl, pathToSaveVideo, "0");
            server = new LocalFileStreamingServer(new File(pathToSaveVideo), activity, VideoStreamAndDownload.this, videoUrl, pathToSaveVideo, "0");
            server.setSupportPlayWhileDownloading(true);
//        } else if (download_status == 1) {
//            server = new LocalFileStreamingServer(file, activity, VideoStreamAndDownload.this);
//            server.setSupportPlayWhileDownloading(false);
        } else {
            Log.d("sachin", "file not null");
            Log.d("sachin", "file size " + file.length());
            // new VideoDownloader(activity).execute(videoUrl, pathToSaveVideo, String.valueOf(file.length()));
            server = new LocalFileStreamingServer(file, activity, VideoStreamAndDownload.this, videoUrl, pathToSaveVideo, String.valueOf(file.length()));
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
                        Log.d("sachin", server.getFileUrl());

                        videoView.setMediaController(mediaController);
                        videoView.setKeepScreenOn(true);
                        videoView.setVideoPath(server.getFileUrl());
                        videoView.start();
                        videoView.requestFocus();

                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                            }
                        });
                    }
                });
            }
        }).start();

    }


    @Override
    public void pauseVideo() {
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        Log.e("sachin", "paused");
        videoView.pause();
        stopPosition = videoView.getCurrentPosition();
//            }
//        });
    }

    @Override
    public void playVideo() {
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        Log.e("sachin", "resumed");
        videoView.seekTo(stopPosition);
        videoView.start();
//            }
//        });
    }


    public void saveInstanceState() {
        long currentPosition = videoView.getCurrentPosition();


    }

    public void restoreInstanceState() {

    }
}
