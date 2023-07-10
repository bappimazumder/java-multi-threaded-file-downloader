/**
 * @version 1.0
 * @author Bappi Mazumder
 * @since 7/10/2023
 * Project Name : java-multi-threaded-file-downloader
 */

package downloader;
/*
* This is the main class to test the download file in multi-threaded environment
* */
public class FileDownloaderMain {
    public static void main(String[] args) {
        System.out.println("File Downloading....");
        final String DOWNLOAD_DIR = "./";
        final String URL =  "http://techslides.com/demos/sample-videos/small.mp4";
        DownloadManager downloadManager = new DownloadManager();
        downloadManager.download(URL,DOWNLOAD_DIR);
        System.out.println("File Download Complete !");
    }
}
