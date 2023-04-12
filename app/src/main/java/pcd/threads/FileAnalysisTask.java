package pcd.threads;

import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileAnalysisTask implements Runnable {

    private final List<File> files;
    private final FileMonitor fileMonitor;
    private final LinesRangeMonitor linesRangeMonitor;

    public FileAnalysisTask(List<File> files, FileMonitor fileMonitor, LinesRangeMonitor linesRangeMonitor) {
        this.files = files;
        this.fileMonitor = fileMonitor;
        this.linesRangeMonitor = linesRangeMonitor;
    }

    public void run() {
        for(File file: this.files) {
            long lines = this.getFileLineCount(file);
            this.fileMonitor.addEntry(file, lines);
            this.linesRangeMonitor.incRangeCount(lines);
        }
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
