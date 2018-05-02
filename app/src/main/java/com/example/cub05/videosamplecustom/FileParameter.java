package com.example.cub05.videosamplecustom;

import java.io.Serializable;

/**
 * Created by cub05 on 5/1/2018.
 */

public class FileParameter implements Serializable {
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileLength() {
        return fileLength;
    }

    public void setFileLength(String fileLength) {
        this.fileLength = fileLength;
    }

    private String filePath;
    private String fileLength;

    public FileParameter(String filePath, String fileLength) {
        this.filePath = filePath;
        this.fileLength = fileLength;
    }
}
