package pcd.gui;

import java.util.ArrayList;

import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;
import pcd.threads.DirectoryAnalysisTask;
import pcd.threads.executor.ExecutorService;
import pcd.threads.executor.Executors;
import pcd.utils.Chrono;

public class Model {

    private final ArrayList<AnalyzerEventListener> listeners;
    private final FileMonitor fileMonitor;
    private final LinesRangeMonitor linesRangeMonitor;
    private ExecutorService service;

    public Model(FileMonitor fileMonitor, LinesRangeMonitor linesRangeMonitor) {
        this.listeners = new ArrayList<>();
        this.fileMonitor = fileMonitor;
        this.linesRangeMonitor = linesRangeMonitor;
    }

    protected synchronized ExecutorService getService() {
        return this.service;
    }

    protected synchronized void startAnalysis(String pathDir) {
        new Thread(() -> {
            Chrono cron = new Chrono();
            for(AnalyzerEventListener l: listeners) {
                l.analysisProcessStarted();
            }
            this.fileMonitor.clearSet();    // Clear map before start
            cron.start();
            service = Executors.newFixedThreadPool(3*Runtime.getRuntime().availableProcessors());
            service.execute(new DirectoryAnalysisTask(pathDir, this.fileMonitor, this.linesRangeMonitor, service));
            service.joinAll();
            cron.stop();
            for(AnalyzerEventListener l: listeners) {
                l.analysisProcessFinished(cron.getTime());
            }
        }).start();
    }

    public synchronized void addListener(AnalyzerEventListener l) {
        this.listeners.add(l);
    }
}
