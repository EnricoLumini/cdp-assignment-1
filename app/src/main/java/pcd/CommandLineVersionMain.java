package pcd;

import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;
import pcd.threads.DirectoryAnalysisTask;
import pcd.threads.executor.ExecutorService;
import pcd.threads.executor.Executors;
import pcd.utils.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineVersionMain {

    private static String rootDir;
    private static int N;
    private static int NI;
    private static int MAXL;

    public static void main(String[] args) {
        parseArgs(args);
        FileMonitor fileMonitor = new FileMonitor();
        LinesRangeMonitor linesRangeMonitor = new LinesRangeMonitor(NI, MAXL);
        int nCore = Runtime.getRuntime().availableProcessors();
        System.out.println("[Log] Using " + nCore + " core and " + 3 * nCore + "threads");
        ExecutorService service = Executors.newFixedThreadPool( 3 * nCore);

        Chrono cron = new Chrono();
        cron.start();
        service.execute(new DirectoryAnalysisTask(rootDir, fileMonitor, linesRangeMonitor, service));

        service.joinAll();
        cron.stop();

        Set<FilePair<String, Long>> fileSet = fileMonitor.getSet();
        System.out.println("[Log] Number of files found: " + fileSet.size());

        // Display table
        N = Math.min(N, fileSet.size());
        FancyTable fancyTable = new FancyTable(fileSet.stream().toList().subList(0, N));
        fancyTable.draw();


        // Display distribution
        System.out.println("\nDistribution of files by line count");
        System.out.println("------------------------------------");
        FancyBarChart fancyBarChart = new FancyBarChart(MAXL, fileSet.size(), linesRangeMonitor.getRangeMap());
        fancyBarChart.draw();

        System.out.println();
        System.out.println("[Log] Time elapsed: " + cron.getTime() + " ms.");
    }

    private static void parseArgs(String[] args) {
        try {
            rootDir = args[0];
            if(!new File(rootDir).exists()) {
                System.out.println("[Err] No such directory " + rootDir);
                System.exit(-1);
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("No arguments specified, syntax is ROOT_DIR [N NI MAXL]");
            System.exit(-1);
        }

        Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
        Matcher matcher;
        try {
            matcher = pattern.matcher(args[1]);
            if (!matcher.matches()) {
                System.err.println("Argument" + args[1] + " must be a positive and not null integer");
            } else {
                N = Integer.parseInt(args[1]);
                try {
                    matcher = pattern.matcher(args[2]);
                    if (!matcher.matches()) {
                        System.err.println("Argument" + args[2] + " must be a positive and not null integer");
                    } else {
                        NI = Integer.parseInt(args[2]);
                        try {
                            matcher = pattern.matcher(args[3]);
                            if(!matcher.matches()) {
                                System.err.println("Argument" + args[3] + " must be a positive and not null integer");
                            } else {
                                MAXL = Integer.parseInt(args[3]);
                                System.out.println("[Log] Starting analysis in directory: " + rootDir +
                                        String.format("with parameters N=%d, NI=%d, MAXL=%d", N, NI, MAXL));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            System.err.println("Incorrect number of parameters, syntax is ROOT_DIR [N NI MAXL]");
                            System.exit(-1);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("Incorrect number of parameters, syntax is ROOT_DIR [N NI MAXL]");
                    System.exit(-1);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("[Log] Starting analysis in directory: " + rootDir);
            System.out.println("[Wrn] no args specified for parameters N, NI and MAXL, using defaults (N=10, NI=5, MAXL=100)");
            N = 10;
            NI = 5;
            MAXL = 100;
        }
    }
}
