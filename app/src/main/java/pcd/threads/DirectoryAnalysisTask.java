package pcd.threads;

import org.apache.commons.io.FilenameUtils;
import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;
import pcd.threads.executor.ExecutorService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryAnalysisTask implements Runnable {

    private static final int MAX_FILE_NUMBER_FOR_SUBMIT = 1000;
    private static final List<String> parsableFileExtensions = Arrays.asList("java", "c", "cpp", "py", "sh", "txt", "scala");

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
            try {
                long lines = getFileLineCount(file);
                this.fileMonitor.addEntry(file, lines);
                this.linesRangeMonitor.incRangeCount(lines);
            } catch (Exception e) {
                System.out.println("[Err] Error reading file " + file + ":" + e.getMessage());
            }
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
                    .filter(file -> file.isFile() && isAParsableFile(file)).collect(Collectors.toList());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private long getFileLineCount(File file) {
        try {
            return Files.lines(Paths.get(file.getPath()), StandardCharsets.ISO_8859_1).count();
        } catch (IOException e) {
            System.out.println("Error for file " + file.getPath() + ":" + e.getMessage());
        }
        return 0;
    }

    private boolean isAParsableFile(File file) {
        return parsableFileExtensions.contains(FilenameUtils.getExtension(file.getPath()));
    }
}
