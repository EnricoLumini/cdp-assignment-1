package pcd.threads;

import org.apache.commons.io.FilenameUtils;
import pcd.threads.executor.ExecutorService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class DirectoryAnalysisTaskWithExecutorService implements Runnable {

    private static final long MIN_FILE_SIZE_FOR_THREAD_SPAWN = 100000;

    private final String path;
    private final Map<String, Long> filesLineCount;
    private final ExecutorService service;

    public DirectoryAnalysisTaskWithExecutorService(String path, Map<String, Long> filesLineCount, ExecutorService service) {
        this.path = path;
        this.filesLineCount = filesLineCount;
        this.service = service;
    }

    @Override
    public void run() {
        File rooDir = new File(this.path);
        Stream<String> dirs = this.getDirs(rooDir);
        dirs.forEach(dir -> this.service.execute(new DirectoryAnalysisTaskWithExecutorService(dir, this.filesLineCount, this.service)));


        Stream<File> files = this.getFiles(rooDir);
        files.forEach(file -> {
            long fileSize = this.getFileSize(file);
            if (fileSize > MIN_FILE_SIZE_FOR_THREAD_SPAWN) {
                this.service.execute(new FileAnalysisTaskWithExecutorService(file.getPath(), this.filesLineCount));
            } else {
                long lines = getFileLineCount(file);
                this.filesLineCount.put(file.getPath(), lines);
            }
        });
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

    private Stream<File> getFiles(File rootDir) {
        try {
            return Arrays.stream(Objects.requireNonNull(rootDir.listFiles()))
                    .filter(file -> file.isFile() && FilenameUtils.getExtension(file.getPath()).equals("java"));
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

    private long getFileSize(File file) {
        try {
            return Files.size(Paths.get(file.getPath()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

}
