package com.example.cub05.videosamplecustom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cub05 on 4/25/2018.
 */

public class VideoDownloader extends AsyncTask<String, Integer, Void> {
    public static final int DATA_READY = 1;
    public static final int DATA_NOT_READY = 2;
    public static final int DATA_CONSUMED = 3;
    public static final int DATA_NOT_AVAILABLE = 4;
    private Context context;


    SharedPreferences sharedpreferences;

    public VideoDownloader(Context context) {
        this.context = context;
        sharedpreferences = context.getSharedPreferences("FilePref", Context.MODE_PRIVATE);
    }

    public static int dataStatus = -1;

    public static boolean isDataReady() {
        dataStatus = -1;
        boolean res = false;
        if (fileLength == readb) {
            dataStatus = DATA_CONSUMED;
            res = false;
        } else if (readb > consumedb) {
            dataStatus = DATA_READY;
            res = true;
        } else if (readb <= consumedb) {
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
    public static int consumedb = 0;

    /**
     * Keeps track of all bytes read on each while iteration
     */
    private static int readb = 0;

    /**
     * Length of file being downloaded.
     */
    static int fileLength = -1;

    @Override
    protected Void doInBackground(String... params) {


        long fileSizeInLocalStorage = Long.valueOf(params[2]);
        BufferedInputStream input = null;
        try {
            final FileOutputStream out = new FileOutputStream(params[1], true);

            try {
                URL url = new URL(params[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=" + fileSizeInLocalStorage + "-");
                connection.connect();

                Log.e("response code- ", " " + connection.getResponseCode());

                fileLength = connection.getContentLength();


                Log.e("file length-", "" + fileLength);
                Log.e("file length local -", "" + fileSizeInLocalStorage);

                if (connection.getResponseCode() == 416) {
                    readb = (int) fileSizeInLocalStorage;
                    Log.d("sachin", "video is already downloaded");
                } else {
                    input = new BufferedInputStream(connection.getInputStream());
                    byte data[] = new byte[1024 * 50];
                    long readBytes = 0;
                    int len;
                    boolean flag = true;

                    while ((len = input.read(data)) != -1) {
                        out.write(data, 0, len);
                        out.flush();
                        readBytes += len;
                        readb += len;
                        Log.w("download", (readb) + "b of " + (fileLength) + "b");
                    }
                }
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
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        sharedpreferences.edit().putInt("download_status", 1).apply();
        Log.e("shared VDownldr onPost", sharedpreferences.getInt("download_status", -1) + "");
        Log.w("download", "Done");

    }
}
