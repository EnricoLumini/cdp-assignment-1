package pcd;

import pcd.monitor.FileMonitor;
import pcd.monitor.LinesRangeMonitor;
import pcd.threads.DirectoryAnalysisTaskWithExecutorService;
import pcd.threads.executor.ExecutorService;
import pcd.threads.executor.Executors;
import pcd.utils.*;

import java.io.File;
import java.util.*;

public class MainWithExecutorService {

    /**
     * Files per folder:
     * UltimateDirectory -> 3349365  WORKING!!
     * TestFolder2 -> 21865 (1 not .java) WORKING
     */
    public static final String ROOT_DIR = "/home/enrico/Desktop/TestFolder2";

    public static void main(String[] args) {
        int N = 50;
        int NI = 5;
        int MAXL = 200;
        FileMonitor fileMonitor = new FileMonitor();
        LinesRangeMonitor linesRangeMonitor = new LinesRangeMonitor(NI, MAXL);
        int nCore = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + nCore + " core");
        ExecutorService service = Executors.newFixedThreadPool( nCore+ 1); // n_core + 1

        // Test if ROOT_DIR exists
        if(!new File(ROOT_DIR).exists()) {
            System.out.println("[Err] No such directory " + ROOT_DIR);
            System.exit(-1);
        }

        Chrono cron = new Chrono();
        cron.start();
        service.execute(new DirectoryAnalysisTaskWithExecutorService(ROOT_DIR, fileMonitor, linesRangeMonitor, service));

        service.joinAll();

        Set<FilePair<String, Long>> fileSet = fileMonitor.getSet();
        System.out.println("[Log] Number of files found: " + fileSet.size());

        N = Math.min(N, fileSet.size());
        FancyTable fancyTable = new FancyTable(fileSet.stream().toList().subList(0, N));
        fancyTable.draw();


        // Display distribution
        System.out.println("\nDistribution of files by line count");
        System.out.println("------------------------------------");
        FancyBarChart fancyBarChart = new FancyBarChart(MAXL, fileSet.size(), linesRangeMonitor.getRangeMap());
        fancyBarChart.draw();

        System.out.println();
        cron.stopAndPrintTime();
    }
}
