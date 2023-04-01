package pcd.threads;

import pcd.monitor.FileMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileAnalysisTaskWithExecutorService extends Thread{

    private final String path;
    //private final Map<String, Long> filesLineCount;
    private final FileMonitor fileMonitor;

//    public FileAnalysisTaskWithExecutorService(String path, Map<String, Long> filesLineCount) {
//        this.path = path;
//        this.filesLineCount = filesLineCount;
//    }

    public FileAnalysisTaskWithExecutorService(String path, FileMonitor fileMonitor) {
        this.path = path;
        this.fileMonitor = fileMonitor;
    }

    public void run() {
        File file = new File(this.path);
        long lines = this.getFileLineCount(file);
        //this.filesLineCount.put(file.getPath(), lines);
        this.fileMonitor.addEntry(file, lines);
    }

    private long getFileLineCount(File file) {
        try {
            return Files.lines(Paths.get(file.getPath())).parallel().count();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

}
