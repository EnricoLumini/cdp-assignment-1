package pcd.gui;

public interface AnalyzerEventListener {

    void analysisProcessStarted();
    void analysisProcessFinished(long eta);

}
