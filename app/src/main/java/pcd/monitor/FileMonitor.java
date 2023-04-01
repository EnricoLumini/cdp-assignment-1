package pcd.monitor;

import pcd.utils.FilePair;
import java.io.File;
import java.util.*;

public class FileMonitor {

    private final TreeSet<FilePair<String, Long>> filesLineCount;

    public FileMonitor() {
        this.filesLineCount = new TreeSet<>((o1, o2) -> {
            int countCompare = o2.getLines().compareTo(o1.getLines());
            if (countCompare == 0) {
                return o2.getFilePath().compareTo(o1.getFilePath());
            }
            return countCompare;
        });
    }

    public synchronized void addEntry(File file, long lines) {
        FilePair<String, Long> pair = new FilePair<>(file.getPath(), lines);
        this.filesLineCount.add(pair);
    }

    public synchronized void clearSet() {
        this.filesLineCount.clear();
    }

    public synchronized TreeSet<FilePair<String, Long>> getSet() {
        return (TreeSet<FilePair<String, Long>>) this.filesLineCount.clone();
    }
}
