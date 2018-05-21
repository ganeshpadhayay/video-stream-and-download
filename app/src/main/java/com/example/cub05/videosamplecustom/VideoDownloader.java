package com.example.cub05.videosamplecustom;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoDownloader implements Runnable {

    private VideoDownloaderCallbacks videoDownloaderCallbacks;
    private String videoFileUrl;
    private String pathToSaveVideo;
    private long fileLengthInStorage;


    public VideoDownloader(VideoDownloaderCallbacks videoDownloaderCallbacks, String videoFileUrl, String pathToSaveVideo, long fileLengthInStorage) {
        this.videoDownloaderCallbacks = videoDownloaderCallbacks;
        this.fileLengthInStorage = fileLengthInStorage;
        this.videoFileUrl = videoFileUrl;
        this.pathToSaveVideo = pathToSaveVideo;

    }

    public void setFileLengthInStorage(long fileLengthInStorage) {
        this.fileLengthInStorage = fileLengthInStorage;
    }

    @Override
    public void run() {

        BufferedInputStream inputStream = null;

        try {
            final FileOutputStream out = new FileOutputStream(pathToSaveVideo, true);
//            Log.e("VideoDownloader ", "file created in video downloader");

            try {

                URL url = new URL(videoFileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=" + fileLengthInStorage + "-");
                connection.connect();

//                Log.e("server response code- ", " " + connection.getResponseCode());


                if (connection.getResponseCode() == 416) {
                    Log.d("VideoDownloader", "video is already downloaded");
                } else if (connection.getResponseCode() == 200 || connection.getResponseCode() == 206) {

                    inputStream = new BufferedInputStream(connection.getInputStream());
                    byte dataBuffer[] = new byte[1024 * 10];
                    int numberOfBytesRead;
                    int totalReadBytes = (int) fileLengthInStorage;

                    while (!Thread.currentThread().isInterrupted() && (numberOfBytesRead = inputStream.read(dataBuffer)) != -1) {
                        out.write(dataBuffer, 0, numberOfBytesRead);
                        out.flush();
                        totalReadBytes += numberOfBytesRead;
                        Log.w("VideoDownloader", (totalReadBytes) + "b of " + (connection.getContentLength()) + "b downloaded");
                    }
                } else {
                    videoDownloaderCallbacks.onVideoDownloadServerError(connection.getResponseMessage());
                }

                videoDownloaderCallbacks.onVideoDownloaded();


            } catch (MalformedURLException e) {
                videoDownloaderCallbacks.onUrlException("Url Error");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (inputStream != null)
                    inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public interface VideoDownloaderCallbacks {
        void onVideoDownloaded();

        void onVideoDownloadServerError(String responseMessage);

        void onUrlException(String urlError);
    }
}
