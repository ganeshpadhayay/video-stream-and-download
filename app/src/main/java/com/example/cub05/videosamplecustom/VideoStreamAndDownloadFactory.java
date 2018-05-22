package com.example.cub05.videosamplecustom;

import android.app.Activity;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by cub05 on 5/22/2018.
 */

public class VideoStreamAndDownloadFactory {

    public static LocalFileStreamingServer getServer(File file, String videoUrl, String pathToSaveVideo, long fileLength, VideoStreamAndDownload.ProgressBarCallbacks progressBarCallbacks) {
        return new LocalFileStreamingServer(file, videoUrl, pathToSaveVideo, fileLength, progressBarCallbacks);
    }

    public static VideoDownloader getVideoDownloader(VideoDownloader.VideoDownloaderCallbacks videoDownloaderCallbacks, String videoFileUrl, String pathToSaveVideo, long fileLengthInStorage) {
        return new VideoDownloader(videoDownloaderCallbacks, videoFileUrl, pathToSaveVideo, fileLengthInStorage);
    }

    public static VideoStreamAndDownload getVideoStreamAndDownloadObject(MediaController mediaController, VideoView videoView, Activity activity) {
        return new VideoStreamAndDownload(mediaController, videoView, activity);
    }


}
