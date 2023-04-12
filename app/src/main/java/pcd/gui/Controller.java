package pcd.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pcd.gui.update_timer.UpdateTimer;
import pcd.gui.update_timer.UpdateTimerAgent;
import pcd.gui.update_timer.UpdateTimerEventListener;
import pcd.gui.update_timer.UpdateTimerFlag;
import pcd.gui.utils.BarChartWithText;
import pcd.monitors.FileMonitor;
import pcd.monitors.LinesRangeMonitor;
import pcd.utils.FilePair;
import pcd.utils.Range;
import pcd.utils.SortUtils;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable, AnalyzerEventListener, UpdateTimerEventListener {

    private static final int DEFAULT_NUMBER_OF_FILE_TO_DISPLAY = 10;
    private static final int DEFAULT_NUMBER_OF_RANGES = 5;
    private static final int DEFAULT_NUMBER_OF_MAX_LINES_FOR_RANGES = 200;
    private static final String DEFAULT_DIRECTORY_TO_ANALYZE = "/home/enrico/Desktop/TestFolder2";
    public static final int UPDATE_TIME = 500;

    private enum LogLevel {DEBUG, INFO, WARN, ERROR}

    @FXML private Pane rootPane;
    @FXML private Button selectDirectoryButton;
    @FXML private Button startButton;
    @FXML private TextField dirToAnalyze;
    @FXML private TextField nOfFileToDisplay;
    @FXML private TextField nOfRanges;
    @FXML private TextField nOfMaxLinesForRanges;
    @FXML private TextArea logTextArea;
    @FXML private TableView<FilePair<String, Long>> maxLinesFileTable;
    @FXML private ImageView chargingimageView;
    private BarChartWithText<String, Number> linesCountBarChart;

    private Model model;
    private int numberOfFileToDisplay;
    private int numberOfRanges;
    private int maxLinesForRanges;
    private boolean isRunning;

    private FileMonitor fileMonitor;
    private LinesRangeMonitor linesRangeMonitor;

    private UpdateTimer updateTimer;
    private UpdateTimerFlag stopFlag;

    public FileMonitor getFileMonitor() {
        return this.fileMonitor;
    }

    public LinesRangeMonitor getLinesRangeMonitor() {
        return this.linesRangeMonitor;
    }

    public int getNumberOfFileToDisplay() {
        return this.numberOfFileToDisplay;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.startButton.setDisable(true);
        this.numberOfFileToDisplay = DEFAULT_NUMBER_OF_FILE_TO_DISPLAY;

        this.dirToAnalyze.setText(DEFAULT_DIRECTORY_TO_ANALYZE);
        this.nOfFileToDisplay.setText(String.valueOf(DEFAULT_NUMBER_OF_FILE_TO_DISPLAY));
        this.nOfRanges.setText(String.valueOf(DEFAULT_NUMBER_OF_RANGES));
        this.nOfMaxLinesForRanges.setText(String.valueOf(DEFAULT_NUMBER_OF_MAX_LINES_FOR_RANGES));

        // Set table callback for data insertion
        TableColumn<FilePair<String, Long>, String> column1 = (TableColumn<FilePair<String, Long>, String>) this.maxLinesFileTable.getColumns().get(0);
        column1.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilePath()));
        TableColumn<FilePair<String, Long>, String> column2 = (TableColumn<FilePair<String, Long>, String>) this.maxLinesFileTable.getColumns().get(1);
        column2.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLines().toString()));
        this.initTable();

        // Set chart
        this.linesCountBarChart = new BarChartWithText<>(new CategoryAxis(), new NumberAxis());
        this.linesCountBarChart.setLayoutX(507);
        this.linesCountBarChart.setLayoutY(198);
        this.linesCountBarChart.setPrefWidth(480);
        this.linesCountBarChart.setPrefHeight(400);
        this.linesCountBarChart.setAnimated(false);
        this.rootPane.getChildren().add(this.linesCountBarChart);
        this.initBarChart();

        // Set directory chooser button image
        Image img = new Image(this.getClass().getClassLoader().getResource("images/folderIcon.png").toString());
        this.selectDirectoryButton.setGraphic(new ImageView(img));

        // Set image view gif
        Image gif = new Image(this.getClass().getClassLoader().getResource("images/loading.gif").toString());
        this.chargingimageView.setImage(gif);
        this.chargingimageView.setVisible(false);

        // Set model
        this.fileMonitor = new FileMonitor();
        this.linesRangeMonitor = new LinesRangeMonitor(DEFAULT_NUMBER_OF_RANGES, DEFAULT_NUMBER_OF_MAX_LINES_FOR_RANGES);
        this.model = new Model(this.fileMonitor, this.linesRangeMonitor);
        this.model.addListener(this);

        // set timer for table and chart update
        this.updateTimer = new UpdateTimer(UPDATE_TIME, this);
        this.stopFlag = new UpdateTimerFlag();

        this.isRunning = false;

        log("Initialization done", LogLevel.INFO);
        this.startButton.setDisable(false);
    }

    @FXML
    private void selectDirectory(ActionEvent ev) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        // Disable rootPane
        this.rootPane.setDisable(true);
        // New window for directory chooser
        Stage stage = new Stage();
        File chosenDirectory = directoryChooser.showDialog(stage);
        if (!(chosenDirectory == null)) {
            this.dirToAnalyze.setText(chosenDirectory.getPath());
        }
        // Enable rootPane
        this.rootPane.setDisable(false);
    }

    @FXML
    private void startAnalysis() {
        if(this.isRunning) {
            this.startButton.setDisable(true);
            this.isRunning = false;
            log("Analysis stopping...", LogLevel.INFO);
            this.model.getService().disable();
        } else {
            String dirPath = dirToAnalyze.getText();
            if (dirPath != "") {
                if (!new File(dirPath).exists()) {
                    log("No such file or directory " + dirPath, LogLevel.ERROR);
                } else {
                    // check if data is in correct format
                    Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                    if (isFormatCorrect(this.nOfFileToDisplay.getText(), pattern) &&
                            isFormatCorrect(this.nOfRanges.getText(), pattern) &&
                            isFormatCorrect(this.nOfMaxLinesForRanges.getText(), pattern)) {
                        this.numberOfFileToDisplay = Integer.parseInt(this.nOfFileToDisplay.getText());
                        this.numberOfRanges = Integer.parseInt(this.nOfRanges.getText());
                        this.maxLinesForRanges = Integer.parseInt(this.nOfMaxLinesForRanges.getText());

                        this.initBeforeAnalysisStart();
                        this.model.startAnalysis(dirPath);
                    } else {
                        log("Inserted data bad formatted", LogLevel.ERROR);
                    }
                }
            } else {
                log("Must specify a directory to analyze", LogLevel.ERROR);
            }
        }
    }

    @Override
    public void analysisProcessStarted() {
        Platform.runLater(() -> {
            this.isRunning = true;
            this.chargingimageView.setVisible(true);
            this.startButton.setText("Stop");
            log("Analysis process started", LogLevel.INFO);
        });
    }

    @Override
    public void analysisProcessFinished(long eta) {
        Platform.runLater(() -> {
            if (this.isRunning) {
                // Process was running and it's terminated
                this.startButton.setDisable(true);
                this.isRunning = false;
                this.afterAnalysisFinish();
                log("Analysis process finished, found " + this.fileMonitor.getSet().size() + " files in " + eta + " ms", LogLevel.INFO);
                this.startButton.setDisable(false);
            } else {
                // Process was stopped
                this.afterAnalysisFinish();
                log("Analysis stopped, found " + this.fileMonitor.getSet().size() + " files in " + eta + " ms", LogLevel.INFO);
                this.startButton.setDisable(false);
            }
        });
    }

    @Override
    public void updateTable(List<FilePair<String, Long>> items) {
        Platform.runLater(() -> {
            ObservableList<FilePair<String, Long>> tableItems = this.maxLinesFileTable.getItems();
            if(tableItems.size() == 0) {
                tableItems.addAll(items);
            } else {
                tableItems.setAll(items);
            }
        });
    }

    @Override
    public void updateBarChart(XYChart.Series<String, Number> series) {
        Platform.runLater(() -> {
            ObservableList<XYChart.Series<String, Number>> barChartSeries = this.linesCountBarChart.getData();
            series.setName("# of lines");
            if(barChartSeries.size() == 0) {
                barChartSeries.add(series);
            } else {
                barChartSeries.set(0, series);
            }
        });
    }

    public void onExit() {
        Platform.exit();
        System.exit(0);
    }

    private void initBeforeAnalysisStart() {
        this.linesRangeMonitor.setRanges(this.numberOfRanges, this.maxLinesForRanges);
        this.initTable();
        this.initBarChart();
        UpdateTimerAgent updateTimerAgent = new UpdateTimerAgent(this.updateTimer, this.stopFlag);
        this.updateTimer.addListener(this);
        updateTimerAgent.start();
    }

    private void afterAnalysisFinish() {
        this.chargingimageView.setVisible(false);
        this.stopFlag.set();
        this.startButton.setText("Analyze");
        this.displayEndTable();
        this.displayEndBarChart();
    }

    private boolean isFormatCorrect(CharSequence input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private void log(String line, LogLevel logLevel) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:ms");
        String timeStamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        line = line + "\n";
        String lineWithPrefix = line;
        switch (logLevel) {
            case DEBUG:
                lineWithPrefix = "[" + timeStamp + " DEBUG] " + line;
                break;
            case INFO:
                lineWithPrefix = "[" + timeStamp + " INFO] " + line;
                break;
            case WARN:
                lineWithPrefix = "[" + timeStamp + " WARN]" + line;
                break;
            case ERROR:
                lineWithPrefix = "[" + timeStamp + " ERROR]" + line;
                break;
        }
        this.logTextArea.appendText(lineWithPrefix);
    }

    private void displayEndTable() {
        Set<FilePair<String, Long>> set = this.fileMonitor.getSet();
        int n = Math.min(this.numberOfFileToDisplay, set.size());
        List<FilePair<String, Long>> items = set.stream().toList().subList(0, n);
        ObservableList<FilePair<String, Long>> tableItems = this.maxLinesFileTable.getItems();
        if(tableItems.size() == 0) {
            tableItems.addAll(items);
        } else {
            tableItems.setAll(items);
        }
        this.maxLinesFileTable.refresh();
    }

    private void displayEndBarChart() {
        Map<Range, Integer> ranges = SortUtils.sortMapByRange(this.linesRangeMonitor.getRangeMap());
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for(Map.Entry<Range, Integer> entry: ranges.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }
        series.setName("# of lines");
        if (this.linesCountBarChart.getData().size() == 0) {
            this.linesCountBarChart.getData().add(series);
        } else {
            this.linesCountBarChart.getData().set(0, series);
        }
    }

    private void initTable() {
        this.maxLinesFileTable.getItems().clear();
        this.maxLinesFileTable.refresh();
    }

    private void initBarChart() {
        this.linesCountBarChart.getData().clear();
        this.linesCountBarChart.getXAxis().setLabel("Ranges");
    }
}
