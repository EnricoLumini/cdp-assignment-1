package pcd.gui.update_timer;

import javafx.scene.chart.XYChart;
import pcd.gui.Controller;
import pcd.monitors.FileMonitor;
import pcd.utils.Chrono;
import pcd.utils.FilePair;
import pcd.utils.Range;
import pcd.utils.SortUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UpdateTimerAgent extends Thread{

    private final UpdateTimer updateTimer;
    private final UpdateTimerFlag stopFlag;
    private final Chrono crono;
    private final Controller controller;

    public UpdateTimerAgent(UpdateTimer updateTimer, UpdateTimerFlag stopFlag) {
        this.updateTimer = updateTimer;
        this.stopFlag = stopFlag;
        this.crono = new Chrono();
        this.controller = this.updateTimer.getController();
    }

    @Override
    public void run() {
        stopFlag.reset();
        while (!stopFlag.isSet()){
            try {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                List<FilePair<String, Long>> items;
                crono.start();
                FileMonitor fileMonitor = this.controller.getFileMonitor();
                int numberOfFileToDisplay = this.controller.getNumberOfFileToDisplay();
                Set<FilePair<String, Long>> set = fileMonitor.getSet();
                int n = Math.min(numberOfFileToDisplay, set.size());
                items = set.stream().toList().subList(0, n);
                // Update bar chart
                Map<Range, Integer> ranges = SortUtils.sortMapByRange(this.controller.getLinesRangeMonitor().getRangeMap());
                for(Map.Entry<Range, Integer> entry: ranges.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
                }
                crono.stop();
                long eta = crono.getTime();
                Thread.sleep(eta < this.updateTimer.getDelay() ? this.updateTimer.getDelay() - eta : 0);

                // Check if update is still activated, maybe during wait the computation is finished and there is no need of update
                if (!stopFlag.isSet()) {
                    this.updateTimer.expire(items, series);
                }
            } catch(Exception ex){
            }
        }
    }
}
