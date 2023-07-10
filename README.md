# java-multi-threaded-file-downloader
This program demonstrates how to download a file using Java Multi-threading
The algorithm of this program is described below.
- First, we find out the total file size. Then we will take a decision on how many threads will be needed.
- Then divide the file size by thread that will return a chunk size.
Then download all chunks using the thread and then merge all parts into a file.

Technology Used:
Java 8
Java Multi-Threading
Java NIO
