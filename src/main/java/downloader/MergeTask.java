/**
 * @version 1.0
 * @author Bappi Mazumder
 * @since 7/10/2023
 * Project Name : java-multi-threaded-file-downloader
 */

package downloader;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/*
* In this class merge all part of file into a single file and then delete the part file
* */
public class MergeTask implements Runnable{

    public static final String PART_EXTENSION = ".part";
    private String fileName;
    private String downloadDir;
    private int totalParts;
    private CountDownLatch countDownLatch;

    public MergeTask(String fileName, String downloadDir, int totalParts, CountDownLatch countDownLatch) {
        this.fileName = fileName;
        this.downloadDir = downloadDir;
        this.totalParts = totalParts;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();
            File[] files = findPartialFiles();
            Arrays.sort(files);
            File mainFile  = createMainFile();
            mergeFiles(files,mainFile);
            deletePartFiles(files);

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to merge the file "+ e.getMessage());
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private void mergeFiles(File [] partFiles, File finalOutputFile) throws IOException {

        FileChannel outputChannel = new FileOutputStream(finalOutputFile).getChannel();
        for(File partFile : partFiles){
            FileChannel inputChanel = new FileInputStream(partFile).getChannel();
            try {
                inputChanel.transferTo(0,inputChanel.size(),outputChannel);
                inputChanel.close();

            } catch (IOException e) {
                System.err.println("part file can't merge because " + e.getMessage());
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }

    }

    private File createMainFile() throws IOException {
        File mainFile = new File(getPathName());
        if(!mainFile.exists()){
            mainFile.createNewFile();
        }
        return mainFile;
    }


    private File[] findPartialFiles(){
        final File[] files = new File[totalParts];
        for(int i=0;i < files.length;i++){
            files[i] = new File(getDownloadPartName(i));
        }
        return  files;
    }
    private String getDownloadPartName(int partNumber){
        return downloadDir + File.separator + partNumber + PART_EXTENSION;
    }
    private String getPathName(){
        return downloadDir+ File.separator+fileName;
    }

    private void deletePartFiles(File[] files){
        for(File file : files){
            if(file.exists()){
                file.delete();
            }
        }
    }
}
