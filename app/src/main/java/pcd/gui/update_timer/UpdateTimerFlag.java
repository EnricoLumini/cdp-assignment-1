package pcd.gui.update_timer;

public class UpdateTimerFlag {

    private boolean flag;

    public synchronized void reset() {
        flag = false;
    }

    public synchronized void set() {
        flag = true;
    }

    public synchronized boolean isSet() {
        return flag;
    }

}
