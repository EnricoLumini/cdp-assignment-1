package pcd;

import pcd.threads.DirectoryAnalysisTaskWithExecutorService;
import pcd.threads.RangeCalculatorTask;
import pcd.threads.executor.ExecutorService;
import pcd.threads.executor.Executors;
import pcd.utils.Chrono;
import pcd.utils.FancyBarChart;
import pcd.utils.FancyTable;
import pcd.utils.Range;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainWithExecutorService {

    /**
     * Files per folder:
     * UltimateDirectory -> 3349365  WORKING!!
     * TestFolder2 -> 21865 (1 not .java) WORKING
     */
    public static final String ROOT_DIR = "/home/enrico/Desktop/UltimateDirectory";

    public static void main(String[] args) {
        int N = 50;
        Map<String, Long> filesLineCount = new ConcurrentHashMap<>();
        ExecutorService service = Executors.newFixedThreadPool(8 + 1); // n_core + 1

        // Test if ROOT_DIR exists
        if(!new File(ROOT_DIR).exists()) {
            System.out.println("[Err] No such directory " + ROOT_DIR);
            System.exit(-1);
        }

        Chrono cron = new Chrono();
        cron.start();
        service.execute(new DirectoryAnalysisTaskWithExecutorService(ROOT_DIR, filesLineCount, service));

        service.joinAll();

        System.out.println("[Log] Number of files: " + filesLineCount.size());

        // Display first N files with max lines
        System.out.println("\nFirst " + N + " files with max number of lines");
        Comparator<Map.Entry<String, Long>> valueComparator = (o1, o2) -> {
            long v1 = o1.getValue();
            long v2 = o2.getValue();
            return Long.compare(v2, v1);
        };
        List<Map.Entry<String, Long>> listOfEntries = new ArrayList<>(filesLineCount.entrySet());
        listOfEntries.sort(valueComparator);
        N = Math.min(N, filesLineCount.size());

        FancyTable fancyTable = new FancyTable(N, listOfEntries);
        fancyTable.draw();

        Map<Range, Integer> rangeMap = new ConcurrentHashMap<>();
        int MAXL = 100;
        int NI = 10;
        int rangeDimension = MAXL/NI;


        service = Executors.newFixedThreadPool(8 + 1);
        for(int i = 0; i < NI; i++) {
            Range range = Range.between(i*rangeDimension, rangeDimension*(i+1)-1);
            service.execute(new RangeCalculatorTask(range, listOfEntries, rangeMap));
        }
        service.joinAll();

        // Display distribution
        System.out.println("\nDistribution of files by line count");
        System.out.println("------------------------------------");
        FancyBarChart fancyBarChart = new FancyBarChart(MAXL, filesLineCount.size(), rangeMap);
        fancyBarChart.draw();

        System.out.println();
        cron.stopAndPrintTime();
    }
}
