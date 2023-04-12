package pcd.threads.executor;

import java.util.AbstractQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool implements ExecutorService{

    protected static int capacity;
    protected static int currentCapacity;
    protected static LinkedBlockingQueue<Runnable> jobsQueue;
    protected static AbstractQueue<Thread> threadsList;
    private static Executor executor;
    private boolean enabled;

    public ThreadPool(int cp) {
        capacity = cp;
        currentCapacity = 0;
        jobsQueue = new LinkedBlockingQueue<>();
        threadsList = new ConcurrentLinkedQueue<>();
        executor = new Executor();
        this.enabled = true;
    }

    @Override
    public synchronized void execute(Runnable r) {
        // Add to jobs queue only if executor is enabled
        if (this.enabled) {
            jobsQueue.add(r);
            executor.execute();
        }
    }

    @Override
    public void joinAll() {
        while (threadsList.size() > 0) {
            try {
                threadsList.poll().join();
                currentCapacity = currentCapacity - 1;
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void disable() {
        // Disable executor and clear jobs queue
        this.enabled = false;
        jobsQueue.clear();
    }
}
