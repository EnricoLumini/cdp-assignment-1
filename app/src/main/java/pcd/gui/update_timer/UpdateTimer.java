package pcd.gui.update_timer;

import javafx.scene.chart.XYChart;
import pcd.gui.Controller;
import pcd.utils.FilePair;

import java.util.*;

public class UpdateTimer {

    private final ArrayList<UpdateTimerEventListener> listeners;
    private final Controller controller;
    private final long delay;

    public UpdateTimer(long delay, Controller controller) {
        this.listeners = new ArrayList<>();
        this.delay = delay;
        this.controller = controller;
    }

    public synchronized Controller getController() {
        return this.controller;
    }

    public synchronized void expire(List<FilePair<String, Long>> items, XYChart.Series<String, Number> series) {
        // Notify GUI that data is ready
        this.notifyTableUpdate(items);
        this.notifyBarChartUpdate(series);
    }

    public synchronized long getDelay() {
        return this.delay;
    }

    public synchronized void addListener(UpdateTimerEventListener listener) {
        this.listeners.add(listener);
    }

    private void notifyTableUpdate(List<FilePair<String, Long>> items) {
        for (UpdateTimerEventListener l: listeners) {
            l.updateTable(items);
        }
    }

    private void notifyBarChartUpdate(XYChart.Series<String, Number> series){
        for (UpdateTimerEventListener l: listeners){
            l.updateBarChart(series);
        }
    }

}
