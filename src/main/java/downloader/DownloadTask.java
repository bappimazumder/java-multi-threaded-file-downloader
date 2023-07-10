/**
 * @version 1.0
 * @author Bappi Mazumder
 * @since 7/10/2023
 * Project Name : java-multi-threaded-file-downloader
 */

package downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CountDownLatch;
/*
* This class is responsible for download the file into multiple part and save those parts file
* into a directory
* */
public class DownloadTask implements Runnable{

    /* Web address of the file */
    private URL downloadUrl;
    /* Byte starting number for downloading part */
    private long startByte;
    /* size of this part */
    private long partSize;

    /* name of this part */
    private String partName;
    /* download directory of this part */
    private String downloadDir;
    /* This property will inform the others thread that the download completed  */
    private CountDownLatch countDownLatch;

    public DownloadTask(URL downloadUrl, long startByte, long partSize,String partName
                         ,String downloadDir, CountDownLatch countDownLatch) {
        this.downloadUrl = downloadUrl;
        this.startByte = startByte;
        this.partSize = partSize;
        this.partName = partName;
        this.downloadDir = downloadDir;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {

        try{
            final HttpURLConnection connection = getConnection();
            InputStream inputStream = connection.getInputStream();
            /* Here using NIO means Non-blocking IO.
            * For instance, a thread can ask a channel to read data into a buffer.
            * While the channel reads data into the buffer, the thread can do something else.
            * Once data is read into the buffer, the thread can then continue processing it.
            * The same is true for writing data to channels
            * */
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            FileOutputStream outputStream = new FileOutputStream(getPartFileName());
            FileChannel outputChanel = outputStream.getChannel();
            outputChanel.transferFrom(readableByteChannel,0,connection.getContentLength());
            countDownLatch.countDown();
            outputChanel.close();
        } catch (IOException e) {
            System.err.println("Failed to download the selected file"+ e.getMessage());
            Thread.currentThread().interrupt();
            /*An assertion Error is thrown when say "You have written a code that should not
            * execute at all costs because according to you logic it should not happen.
            * BUT if it happens then throw AssertionError. And you don't catch it.
            * In such a case you throw an Assertion error.
            * */
            throw new AssertionError(e);
        }

    }

    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)downloadUrl.openConnection();
        String downloadRange = "bytes="+startByte+"-"+(startByte+partSize);
        connection.setRequestProperty("Range",downloadRange);
        connection.connect();
        return connection;
    }

    private String getPartFileName(){
        return downloadDir + File.separator + partName;
    }

}
