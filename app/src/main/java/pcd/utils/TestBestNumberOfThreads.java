package pcd.utils;

import com.github.sh0nk.matplotlib4j.NumpyUtils;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonConfig;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;
import pcd.threads.DirectoryAnalysisTask;
import pcd.threads.executor.ExecutorService;
import pcd.threads.executor.Executors;
import java.io.IOException;
import java.util.*;

public class TestBestNumberOfThreads {

    private static final String PATH_DIR = "/home/enrico/Desktop/UltimateDirectory";

    private static final Chrono crono = new Chrono();
    private static ExecutorService service;

    private static List<Map.Entry<Integer, Long>> startTest(int minThreadNumber, int maxThreadNumber, int step, int iterations) {
        iterations = Math.max(iterations, 0);
        Map<Integer, Long> times = new HashMap<>();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("Number of available processor's: " + availableProcessors + "\n");
        for(int j = 0; j < iterations; j++) {
            System.out.println("Iteration #" + j);
            for (int i = minThreadNumber; i <= maxThreadNumber; i += step) {
                System.out.println("Testing system with " + i + " threads");
                service = Executors.newFixedThreadPool(i);
                FileMonitor fileMonitor = new FileMonitor();
                crono.start();
                service.execute(new DirectoryAnalysisTask(PATH_DIR, fileMonitor, new LinesRangeMonitor(5, 200), service));
                service.joinAll();
                crono.stop();
                Long time = crono.getTime();
                System.out.println("Found " + fileMonitor.getSet().size() + " files in " + time + "ms, " + (double) i / availableProcessors + " times of total available processor's");
                if(times.containsKey(i)) {
                    Long oldTime = times.get(i);
                    times.put(i, time+oldTime);
                } else {
                    times.put(i, time);
                }
            }
        }
        List<Map.Entry<Integer, Long>> timesList = new ArrayList<>(times.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.naturalOrder())).toList());
        int finalIterations = iterations;
        timesList.replaceAll((e) -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue() / finalIterations));
        return timesList;
    }

    public static void main(String[] args) {
        int minThreadNumber = 4;
        int maxThreadNumber = 5*Runtime.getRuntime().availableProcessors();
        int step = 4;
        int iterations = 10;
        List<Map.Entry<Integer, Long>> times = TestBestNumberOfThreads.startTest(minThreadNumber, maxThreadNumber, step, iterations);
        System.out.println(times);
        Map.Entry<Integer, Long> bestNumberOThreadEntry = times.stream().min(Map.Entry.comparingByValue()).get();
        System.out.println("Best number of threads: " + bestNumberOThreadEntry.getKey());

        // Plot with matplot
        List<Double> x = NumpyUtils.linspace(minThreadNumber, times.get(times.size() - 1).getKey(), times.size());
        List<Long> y = times.stream().map(Map.Entry::getValue).toList();
        Plot plt = Plot.create(PythonConfig.pythonBinPathConfig("/usr/bin/python3"));
        plt.plot().add(x, y).label("Time/Thread number");
        plt.text(bestNumberOThreadEntry.getKey() - 0.1, bestNumberOThreadEntry.getValue() - 0.3, "*");
        plt.legend().loc("upper right");
        plt.title("Computation time in relation with thread number");
        plt.xlabel("Number of threads");
        plt.ylabel("Time [ms]");
        plt.xlim(minThreadNumber, maxThreadNumber);
        String info = String.format(">Number of core: %d\\n>Min thread number: %d\\n>Max thread number: %d\\n>Step: %d\\n>Iterations: %d",
                Runtime.getRuntime().availableProcessors(), minThreadNumber, maxThreadNumber, step, iterations);
        double minX = x.stream().min(Comparator.naturalOrder()).get();
        double maxX = x.stream().max(Comparator.naturalOrder()).get();
        double minY = y.stream().min(Comparator.naturalOrder()).get();
        double maxY = y.stream().max(Comparator.naturalOrder()).get();
        plt.text((minX + maxX) / 2, (minY + maxY) / 2, info);
        try {
            plt.show();
        } catch (PythonExecutionException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOError: " + e.getMessage());
        }
    }
}
