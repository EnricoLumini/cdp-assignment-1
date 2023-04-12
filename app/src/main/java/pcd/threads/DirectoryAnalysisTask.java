package pcd.threads;

import org.apache.commons.io.FilenameUtils;
import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;
import pcd.threads.executor.ExecutorService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryAnalysisTask implements Runnable {

    private static final int MAX_FILE_NUMBER_FOR_SUBMIT = 1000;

    private final String path;
    private final FileMonitor fileMonitor;
    private final LinesRangeMonitor linesRangeMonitor;
    private final ExecutorService service;

    public DirectoryAnalysisTask(String path, FileMonitor fileMonitor, LinesRangeMonitor linesRangeMonitor, ExecutorService service) {
        this.path = path;
        this.linesRangeMonitor = linesRangeMonitor;
        this.fileMonitor = fileMonitor;
        this.service = service;
    }

    @Override
    public void run() {
        File rooDir = new File(this.path);
        Stream<String> dirs = this.getDirs(rooDir);
        dirs.forEach(dir -> this.service.execute(new DirectoryAnalysisTask(dir, this.fileMonitor, this.linesRangeMonitor, this.service)));

        List<File> files = this.getFiles(rooDir);
        int numberOfFiles = files.size();
        int numberOfNewTask = (numberOfFiles / MAX_FILE_NUMBER_FOR_SUBMIT);

        int numberOfFilesToProcess = Math.min(numberOfFiles, MAX_FILE_NUMBER_FOR_SUBMIT);
        List<File> filesToProcess = files.subList(0, numberOfFilesToProcess);
        filesToProcess.forEach(file -> {
            long lines = getFileLineCount(file);
            this.fileMonitor.addEntry(file, lines);
            this.linesRangeMonitor.incRangeCount(lines);
        });
        for(int i = 0; i < numberOfNewTask; i++) {
            // Submit remaining files to other task
            int from = (i + 1) * MAX_FILE_NUMBER_FOR_SUBMIT;
            int to = (i == numberOfNewTask - 1) ? files.size() : (i+2) * MAX_FILE_NUMBER_FOR_SUBMIT;
            this.service.execute(new FileAnalysisTask(files.subList(from, to), this.fileMonitor, this.linesRangeMonitor));
        }
    }

    private Stream<String> getDirs(File rootDir) {
        try {
            return Arrays.stream(Objects.requireNonNull(rootDir.listFiles()))
                    .filter(File::isDirectory)
                    .map(File::getPath);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private List<File> getFiles(File rootDir) {
        try {
            return Arrays.stream(Objects.requireNonNull(rootDir.listFiles()))
                    .filter(file -> file.isFile() && FilenameUtils.getExtension(file.getPath()).equals("java")).collect(Collectors.toList());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private long getFileLineCount(File file) {
        try {
            return Files.lines(Paths.get(file.getPath())).count();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
