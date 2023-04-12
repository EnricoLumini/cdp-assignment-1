package pcd.threads.executor;

public class Executor implements Runnable {

    protected void execute() {
        if (ThreadPool.currentCapacity < ThreadPool.capacity) {
            ThreadPool.currentCapacity = ThreadPool.currentCapacity + 1;
            Thread t = new Thread(new Executor());
            ThreadPool.threadsList.add(t);
            t.start();
        }
    }

    @Override
    public void run() {
        while (ThreadPool.jobsQueue.size() > 0) {
            Runnable task = ThreadPool.jobsQueue.poll();
            if(task != null) {
                task.run();
            }
        }
    }
}
