/**
 * @version 1.0
 * @author Bappi Mazumder
 * @since 7/10/2023
 * Project Name : java-multi-threaded-file-downloader
 */

package downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
* This Class first call download Task and then call the  merge task
* */
public class DownloadManager {
    public void download(String url, String downloadDir){
        if(!isValidUrl(url)){
            throw new IllegalArgumentException("Not a valid url");
        }

        try {
            URL videoDownloadUrl = new URL(url);
            long fullContentSize = getDownloadableContentLength(videoDownloadUrl);
            int numberOfChunk = getTotalChunk();
            long partSize = fullContentSize/numberOfChunk;
            long remainingBytes = fullContentSize%numberOfChunk;
            ExecutorService threadPool = Executors.newFixedThreadPool(numberOfChunk);
            CountDownLatch countDownLatch = new CountDownLatch(numberOfChunk);

            /* File Download part*/
            long startByte = 0;
            for(int chunk = 0;chunk < numberOfChunk;chunk++){
                    long byteCount = partSize;
                    if(byteCount == (numberOfChunk-1)){
                        byteCount += remainingBytes;
                    }
                    String partsName = getPartsName(chunk);
                    DownloadTask downloadTask = new DownloadTask(videoDownloadUrl,startByte,byteCount,
                                                partsName,downloadDir,countDownLatch);
                    startByte = startByte+partSize+1;
                    threadPool.submit(downloadTask);
            }
            /* File merge part */

            String fileName = url.substring(url.lastIndexOf('/')+1,url.length());
            MergeTask mergeTask = new MergeTask(fileName,downloadDir,numberOfChunk,countDownLatch);
            threadPool.submit(mergeTask);
            threadPool.shutdown();
            threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException e) {
            System.err.println("File can't download");
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private String getPartsName(int chunkId){
        return chunkId + MergeTask.PART_EXTENSION;
    }

    private int getTotalChunk(){
        return Runtime.getRuntime().availableProcessors();
    }

    private long getDownloadableContentLength(URL url) throws IOException{

        return url.openConnection().getContentLengthLong();
    }

    private static boolean isValidUrl(String url){
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }


}
