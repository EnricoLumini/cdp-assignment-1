package pcd.threads;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class FileAnalysisTaskWithExecutorService extends Thread{

    private final String path;
    private final Map<String, Long> filesLineCount;

    public FileAnalysisTaskWithExecutorService(String path, Map<String, Long> filesLineCount) {
        this.path = path;
        this.filesLineCount = filesLineCount;
    }

    public void run() {
        File file = new File(this.path);
        long lines = this.getFileLineCount(file);
        this.filesLineCount.put(file.getPath(), lines);
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
