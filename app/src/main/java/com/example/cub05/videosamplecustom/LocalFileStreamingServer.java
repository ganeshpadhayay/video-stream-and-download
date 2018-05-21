package com.example.cub05.videosamplecustom;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A single-connection HTTP server that will respond to requests for files and
 * pull them from the application's SD card.
 */
public class LocalFileStreamingServer implements Runnable, VideoDownloader.VideoDownloaderCallbacks {

    private static final String TAG = LocalFileStreamingServer.class.getName();

    private int port = 0;
    private ServerSocket socket;
    private Thread localStreamingServerThread, videoDownloadingThread;
    private File externalStorageFile;

    private long cbSkip;

    private boolean seekRequest;
    private boolean isRunning = false;
    private boolean supportPlayWhileDownloading = false;
    private boolean videoDownloadingStopped = false;

    private VideoDownloader videoDownloader;
    private VideoStreamAndDownload.ProgressBarCallbacks progressBarCallbacks;

    /**
     * This server accepts HTTP request and returns files from device.
     */
    public LocalFileStreamingServer(File file, String videoUrl, String pathToSaveVideo, long fileLength, VideoStreamAndDownload.ProgressBarCallbacks progressBarCallbacks) {
        this.videoDownloader = new VideoDownloader(LocalFileStreamingServer.this, videoUrl, pathToSaveVideo, fileLength);
        videoDownloadingThread = new Thread(this.videoDownloader);
        videoDownloadingThread.start();
        externalStorageFile = file;
        this.progressBarCallbacks = progressBarCallbacks;
    }

    /**
     * Prepare the server to start.
     * <p>
     * This only needs to be called once per instance. Once initialized, the
     * server can be started and stopped as needed.
     */
    public String init(String ip) {
        String url = null;
        try {
            InetAddress inet = InetAddress.getByName(ip);
            byte[] bytes = inet.getAddress();
            socket = new ServerSocket(port, 0, InetAddress.getByAddress(bytes));

            socket.setSoTimeout(10000);
            port = socket.getLocalPort();
            url = "http://" + socket.getInetAddress().getHostAddress() + ":"
                    + port;
            Log.e(TAG, "Server started at " + url);
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error UnknownHostException server", e);
        } catch (IOException e) {
            Log.e(TAG, "Error IOException server", e);
        }
        return url;
    }

    /**
     * Start the server.
     */
    public void start() {
        localStreamingServerThread = new Thread(this);
        localStreamingServerThread.start();
        isRunning = true;
    }

    /**
     * Stop the server.
     * <p>
     * This stops the localStreamingServerThread listening to the port. It may take up to five
     * seconds to close the service and this call blocks until that occurs.
     */
    public void stop() {
        isRunning = false;
        if (localStreamingServerThread == null) {
            Log.e(TAG, "Server was stopped without being started.");
            return;
        }
        Log.e(TAG, "Stopping server.");
        localStreamingServerThread.interrupt();
    }


    /**
     * This is used internally by the server and should not be called directly.
     */
    @Override
    public void run() {
        Log.e(TAG, "running");
        while (isRunning) {
            try {
                Socket client = socket.accept();
                if (client == null) {
                    continue;
                }
                Log.e(TAG, "client connected at " + port);
                ExternalResourceDataSource data = new ExternalResourceDataSource(externalStorageFile);
                Log.e(TAG, "processing request...");
                processRequest(data, client);
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "No client connected, waiting for client...", e);
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to client", e);
            }
        }
        //TODO- Finish the video View controller here
        Log.e(TAG, "Server interrupted or stopped. Shutting down.");
    }

    /*
     * Sends the HTTP response to the client, including headers (as applicable)
     * and content.
     */
    private void processRequest(ExternalResourceDataSource dataSource, Socket client) throws IllegalStateException, IOException {

        if (dataSource == null) {
            Log.e(TAG, "Invalid (null) resource.");
            client.close();
            return;
        }

        final int bufferSize = 8192;
        byte[] dataBuffer = new byte[bufferSize];
        int readLen = 0;

        readLen = calculateSplitByteForHeaderParsing(client, dataBuffer, readLen, bufferSize);

        // Create a BufferedReader for parsing the header.
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataBuffer, 0, readLen);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

        Properties pre = new Properties();
        Properties params = new Properties();
        Properties header = new Properties();

        try {
            decodeHeader(bufferedReader, pre, params, header);
        } catch (InterruptedException e1) {
            Log.e(TAG, "Exception: " + e1.getMessage());
            e1.printStackTrace();
        }

        for (Map.Entry<Object, Object> e : header.entrySet()) {
            Log.e(TAG, "Header: " + e.getKey() + " : " + e.getValue());
        }

        handleRange(header);

        String headers = "";
        Log.e("sachin", "is seek request: " + seekRequest);
        if (seekRequest) {
            headers = createHeaderForPartialContent(headers, dataSource);
        } else {
            headers = createHeaderForFullContent(headers, dataSource);
        }

        Log.e("sachin- ", headers);

        InputStream dataSourceInputStream = null;
        try {
            dataSourceInputStream = dataSource.createInputStream();
            writeHeadersToClientOutputStream(dataSourceInputStream, headers, client);
            writeContentToClientOutputStream(dataSourceInputStream, client);

        } catch (SocketException e) {
            // Ignore when the client breaks connection
            Log.e(TAG, "Ignoring " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error getting content stream.", e);
        } catch (Exception e) {
            Log.e(TAG, "Error streaming file content.", e);
        } finally {
            if (dataSourceInputStream != null) {
                dataSourceInputStream.close();
            }
            client.close();
        }
    }


    private void writeHeadersToClientOutputStream(InputStream dataSourceInputStream, String headers, Socket client) throws IOException {
        byte[] headerBuffer = headers.getBytes();
        Log.e(TAG, "writing to client");
        client.getOutputStream().write(headerBuffer, 0, headerBuffer.length);
    }

    private void writeContentToClientOutputStream(InputStream dataSourceInputStream, Socket client) throws IOException, InterruptedException {
        byte[] buffer = new byte[1024 * 10];
        Log.e(TAG, "No of bytes skipped: " + dataSourceInputStream.skip(cbSkip));

        while (isRunning) {

            int cbRead = dataSourceInputStream.read(buffer, 0, buffer.length);
            Log.e("sachin", "cbread -" + cbRead);
            if (supportPlayWhileDownloading)
                while (cbRead == -1) {
                    synchronized (this) {
                        Thread.sleep(1000);
                    }
                    cbRead = dataSourceInputStream.read(buffer, 0, buffer.length);
                }
            client.getOutputStream().write(buffer, 0, cbRead);
            client.getOutputStream().flush();

        }

    }

    private String createHeaderForFullContent(String headers, ExternalResourceDataSource dataSource) {
        headers += "HTTP/1.1 200 OK\r\n";
        headers += "Content-Type: " + dataSource.getContentType() + "\r\n";
        headers += "Accept-Ranges: bytes\r\n";
        headers += "Content-Length: " + dataSource.getContentLength(true)
                + "\r\n";
        headers += "\r\n";
        return headers;
    }

    private String createHeaderForPartialContent(String headers, ExternalResourceDataSource dataSource) {
        Log.e("sachin", "in seekRequest if condition");
        if (cbSkip > dataSource.getContentLength(false)) {
//                while (cbSkip > videoDownloader.getReadb()) {
//                    progressBarCallbacks.startProgressbar();
//                    Log.e("sachin ", cbSkip + " cbSkip");
//                    Log.e("sachin ", videoDownloader.getReadb() + " readDb");
//                }
            Log.e("sachin", "before play video");
            progressBarCallbacks.startProgressbar();
        } else {
            progressBarCallbacks.stopProgressbar();
        }

        headers += "HTTP/1.1 206 Partial Content\r\n";
        headers += "Content-Type: " + dataSource.getContentType() + "\r\n";
        headers += "Accept-Ranges: bytes\r\n";
        headers += "Content-Length: " + dataSource.getContentLength(true)
                + "\r\n";
        headers += "Content-Range: bytes " + cbSkip + "-\r\n";
        headers += "\r\n";
        return headers;
    }

    private void handleRange(Properties header) {
        String range = header.getProperty("range");
        cbSkip = 0;
        seekRequest = false;

        if (range != null) {
            Log.e(TAG, "range is: " + range);
            seekRequest = true;
            range = range.substring(6);
            int charPos = range.indexOf('-');
            if (charPos > 0) {
                range = range.substring(0, charPos);
            }
            cbSkip = Long.parseLong(range);
            Log.e(TAG, "range found!! " + cbSkip);
        }
    }

    private int calculateSplitByteForHeaderParsing(Socket client, byte[] dataBuffer, int readlen, int bufferSize) throws IOException {
        InputStream inputStream = client.getInputStream();

        int splitByte = 0;

        {
            int read = inputStream.read(dataBuffer, 0, bufferSize);
            while (read > 0) {
                readlen += read;
                splitByte = findHeaderEnd(dataBuffer, readlen);
                if (splitByte > 0)
                    break;
                read = inputStream.read(dataBuffer, readlen, bufferSize - readlen);
            }
        }

        return readlen;
    }


    /**
     * Find byte index separating header from body. It must be the last byte of
     * the first two sequential new lines.
     **/
    private int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 3 < rlen) {
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n'
                    && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n')
                return splitbyte + 4;
            splitbyte++;
        }
        return 0;
    }

    /**
     * Decodes the sent headers and loads the data into java Properties' key -
     * value pairs
     **/
    private void decodeHeader(BufferedReader in, Properties pre, Properties params, Properties header) throws InterruptedException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null)
                return;
            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens())
                Log.e(TAG,
                        "BAD REQUEST: Syntax error. Usage: GET /example/file.html");

            String method = st.nextToken();
            pre.put("method", method);

            if (!st.hasMoreTokens())
                Log.e(TAG,
                        "BAD REQUEST: Missing URI. Usage: GET /example/file.html");

            String uri = st.nextToken();

            // Decode parameters from the URI
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                decodeParams(uri.substring(qmi + 1), params);
                uri = decodePercent(uri.substring(0, qmi));
            } else
                uri = decodePercent(uri);

            // If there's another token, it's protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            // NOTE: this now forces header names lowercase since they are
            // case insensitive and vary by client.
            if (st.hasMoreTokens()) {
                String line = in.readLine();
                while (line != null && line.trim().length() > 0) {
                    int p = line.indexOf(':');
                    if (p >= 0)
                        header.put(line.substring(0, p).trim().toLowerCase(),
                                line.substring(p + 1).trim());
                    line = in.readLine();
                }
            }

            pre.put("uri", uri);
        } catch (IOException ioe) {
            Log.e(TAG,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        }
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
     * Properties. NOTE: this doesn't support multiple identical keys due to the
     * simplicity of Properties -- if you need multiples, you might want to
     * replace the Properties with a Hashtable of Vectors or such.
     */
    private void decodeParams(String URIString, Properties params) throws InterruptedException {
        if (URIString == null)
            return;

        StringTokenizer st = new StringTokenizer(URIString, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0)
                params.put(decodePercent(e.substring(0, sep)).trim(),
                        decodePercent(e.substring(sep + 1)));
        }
    }

    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     */
    private String decodePercent(String str) throws InterruptedException {
        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch (c) {
                    case '+':
                        sb.append(' ');
                        break;
                    case '%':
                        sb.append((char) Integer.parseInt(
                                str.substring(i + 1, i + 3), 16));
                        i += 2;
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "BAD REQUEST: Bad percent-encoding.");
            return null;
        }
    }


    public String getFileUrl() {
        return "http://" + socket.getInetAddress().getHostAddress() + ":"
                + port + "/" + externalStorageFile.getName();
    }

    /**
     * Determines if the server is running (i.e. has been <code>start</code>ed
     * and has not been <code>stop</code>ed.
     *
     * @return <code>true</code> if the server is running, otherwise
     * <code>false</code>
     */
    public boolean isRunning() {
        return isRunning;
    }


    public void stopVideoDownloading() {
        videoDownloadingThread.interrupt();
        videoDownloadingStopped = true;
    }


    public void startVideoDownloading(long fileLength) {
        if (videoDownloadingThread.isInterrupted() || videoDownloadingStopped) {
            Log.e("sachin", "thread start");
            videoDownloader.setFileLengthInStorage(fileLength);
            videoDownloadingThread = new Thread(videoDownloader);
            videoDownloadingThread.start();
        }
    }


    public void setSupportPlayWhileDownloading(boolean supportPlayWhileDownloading) {
        this.supportPlayWhileDownloading = supportPlayWhileDownloading;
    }


    @Override
    public void onVideoDownloaded() {
        supportPlayWhileDownloading = false;
    }

    @Override
    public void onVideoDownloadServerError(String responseMessage) {
        Log.d(TAG, "server error " + responseMessage);
    }

    @Override
    public void onUrlException(String urlError) {
        Log.d(TAG, urlError);
    }


    /**
     * provides meta-data and access to a stream for resources on Internal Storage.
     */
    protected class ExternalResourceDataSource {

        private final File videoFile;
        private long contentLength;
        private FileInputStream inputStream;

        public ExternalResourceDataSource(File resource) {
            videoFile = resource;
            Log.e(TAG, "resourcePath is: " + resource.getPath());
        }

        /**
         * Returns a MIME-compatible content type (e.g. "text/html") for the
         * resource. This method must be implemented.
         *
         * @return A MIME content type.
         */
        public String getContentType() {
            // TODO: Support other media if we need to
            return "video/mp4";
        }

        /**
         * Creates and opens an input stream that returns the contents of the
         * resource. This method must be implemented.
         *
         * @return An <code>InputStream</code> to access the resource.
         * @throws IOException If the implementing class produces an error when opening
         *                     the stream.
         */
        public InputStream createInputStream() {

            try {
                inputStream = new FileInputStream(videoFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            contentLength = videoFile.length();

            Log.e(TAG, "file exists??" + videoFile.exists()
                    + " and content length is: " + contentLength);

            return inputStream;
        }

        /**
         * Returns the length of resource in bytes.
         * <p>
         * By default this returns -1, which causes no content-type header to be
         * sent to the client. This would make sense for a stream content of
         * unknown or undefined length. If your resource has a defined length
         * you should override this method and return that.
         *
         * @return The length of the resource in bytes.
         */
        public long getContentLength(boolean forHeaders) {
            if (forHeaders) {
                return -1;
            }
            return videoFile.length();
        }


    }

}