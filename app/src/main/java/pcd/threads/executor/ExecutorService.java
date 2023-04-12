package pcd.threads.executor;

public interface ExecutorService {

    void execute(Runnable r);

    void joinAll();

    void disable();
}
