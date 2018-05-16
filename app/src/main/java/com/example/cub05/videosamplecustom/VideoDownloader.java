package com.example.cub05.videosamplecustom;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cub05 on 4/25/2018.
 */

public class VideoDownloader implements Runnable {
    public final int DATA_READY = 1;
    public final int DATA_NOT_READY = 2;
    public final int DATA_NOT_AVAILABLE = 4;
    public final int DATA_CONSUMED = 3;
    private Context context;
    private VideoDownloaderCallbacks videoDownloaderCallbacks;

    private String videoFileUrl;
    private String pathToSaveVideo;
    private long fileLengthInStorage;


    public VideoDownloader(Context context, VideoDownloaderCallbacks videoDownloaderCallbacks, String videoFileUrl, String pathToSaveVideo, long fileLengthInStorage) {
        this.context = context;
        this.videoDownloaderCallbacks = videoDownloaderCallbacks;
        this.fileLengthInStorage = fileLengthInStorage;
        this.videoFileUrl = videoFileUrl;
        this.pathToSaveVideo = pathToSaveVideo;

    }

    public int dataStatus = -1;

    public boolean isDataReady() {
        dataStatus = -1;
        boolean res = false;
        if (fileLength == readb) {
            dataStatus = DATA_CONSUMED;
            res = false;
        } else if (readb > consumedb) {
            dataStatus = DATA_READY;
            res = true;
        } else if ((readb-.01*readb) <= consumedb) {
            dataStatus = DATA_NOT_READY;
            res = false;
        } else if (fileLength == -1) {
            dataStatus = DATA_NOT_AVAILABLE;
            res = false;
        }
        return res;
    }

    /**
     * Keeps track of read bytes while serving to video player client from server
     */
    public int consumedb = 0;

    /**
     * Keeps track of all bytes read on each while iteration
     */
    public int readb = 0;

    /**
     * Length of file being downloaded.
     */
    int fileLength = -1;

//    @Override
//    protected Void doInBackground(String... params) {
//
//
//        long fileSizeInLocalStorage = Long.valueOf(params[2]);
//        BufferedInputStream input = null;
//        try {
//            final FileOutputStream out = new FileOutputStream(params[1], true);
//            Log.e("sachin ","file created in video downloader");
//
//            try {
//                URL url = new URL(params[0]);
//
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Range", "bytes=" + fileSizeInLocalStorage + "-");
//                connection.connect();
//
//                Log.e("server response code- ", " " + connection.getResponseCode());
//
//                fileLength = connection.getContentLength();
//
//                readb= (int) fileSizeInLocalStorage;
//                if (connection.getResponseCode() == 416) {
//                    readb = (int) fileSizeInLocalStorage;
//                    Log.d("sachin", "video is already downloaded");
//                } else {
//                    input = new BufferedInputStream(connection.getInputStream());
//                    byte data[] = new byte[1024 * 50];
//                    long readBytes = 0;
//                    int len;
//                    boolean flag = true;
//
//                    while ((len = input.read(data)) != -1) {
//                        out.write(data, 0, len);
//                        out.flush();
//                        readBytes += len;
//                        readb += len;
//                        Log.w("download", (readb) + "b of " + (fileLength) + "b");
//                    }
//                }
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (out != null) {
//                    out.flush();
//                    out.close();
//                }
//                if (input != null)
//                    input.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
//        Log.e("sachin","on post execute");
//        videoDownloaderCallbacks.onVideoDownloaded();
//    }

    public int getDATA_READY() {
        return DATA_READY;
    }

    public int getDATA_NOT_READY() {
        return DATA_NOT_READY;
    }

    public int getDATA_NOT_AVAILABLE() {
        return DATA_NOT_AVAILABLE;
    }

    public int getDATA_CONSUMED() {
        return DATA_CONSUMED;
    }

    public int getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(int dataStatus) {
        this.dataStatus = dataStatus;
    }

    public int getConsumedb() {
        return consumedb;
    }

    public void setConsumedb(int consumedb) {
        this.consumedb = consumedb;
    }

    public int getReadb() {
        return readb;
    }

    public void setReadb(int readb) {
        this.readb = readb;
    }

    public long getFileLengthInStorage() {
        return fileLengthInStorage;
    }

    public void setFileLengthInStorage(long fileLengthInStorage) {
        this.fileLengthInStorage = fileLengthInStorage;
    }

    @Override
    public void run() {
        long fileSizeInLocalStorage = fileLengthInStorage;
        BufferedInputStream input = null;
        try {
            final FileOutputStream out = new FileOutputStream(pathToSaveVideo, true);
            Log.e("sachin ", "file created in video downloader");

            try {
                URL url = new URL(videoFileUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=" + fileSizeInLocalStorage + "-");
                connection.connect();

                Log.e("server response code- ", " " + connection.getResponseCode());

                fileLength = connection.getContentLength();

                readb = (int) fileSizeInLocalStorage;
                if (connection.getResponseCode() == 416) {
                    readb = (int) fileSizeInLocalStorage;
                    Log.d("sachin", "video is already downloaded");
                } else {
                    input = new BufferedInputStream(connection.getInputStream());
                    byte data[] = new byte[1024 * 50];
                    long readBytes = 0;
                    int len;
                    boolean flag = true;

                    while (!Thread.currentThread().isInterrupted() && (len = input.read(data)) != -1) {
                        out.write(data, 0, len);
                        out.flush();
                        readBytes += len;
                        readb += len;
                        Log.w("download", (readb) + "b of " + (fileLength) + "b");
                    }
                }
                videoDownloaderCallbacks.onVideoDownloaded();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (input != null)
                    input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean cancel(boolean b) {
        return false;
    }


    public void onNetworkChanged(boolean b) {

    }

    public interface VideoDownloaderCallbacks {
        public void onVideoDownloaded();
    }
}
