package pcd.gui.update_timer;

import javafx.scene.chart.XYChart;
import pcd.utils.FilePair;

import java.util.List;

public interface UpdateTimerEventListener {

    void updateTable(List<FilePair<String, Long>> items);
    void updateBarChart(XYChart.Series<String, Number> series);
}
