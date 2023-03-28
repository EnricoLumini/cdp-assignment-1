package pcd.threads.executor;

public class Executors {

    public static ExecutorService newFixedThreadPool(int capacity) {
        return new ThreadPool(capacity);
    }

}
