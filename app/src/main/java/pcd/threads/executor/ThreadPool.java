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

    public ThreadPool(int cp) {
        capacity = cp;
        currentCapacity = 0;
        jobsQueue = new LinkedBlockingQueue<>();
        threadsList = new ConcurrentLinkedQueue<>();
        executor = new Executor();
    }

    @Override
    public void execute(Runnable r) {
        jobsQueue.add(r);
        executor.execute();
    }

    @Override
    public void joinAll() {
        while (threadsList.size() > 0) {
            try {
                threadsList.poll().join();
                //executor.removeFromThreadsList(thread);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
