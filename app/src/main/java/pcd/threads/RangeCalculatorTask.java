package pcd.threads;

import pcd.utils.Range;

import java.util.List;
import java.util.Map;

public class RangeCalculatorTask implements Runnable {

    private final Map<Range, Integer> rangeMap;
    private final List<Map.Entry<String, Long>> entries;
    private final Range range;

    public RangeCalculatorTask(Range range, List<Map.Entry<String, Long>> entries, Map<Range, Integer> rangeMap) {
        this.range = range;
        this.entries = entries;
        this.rangeMap = rangeMap;
    }

    public void run() {
        int counter=0;
        for(Map.Entry<String, Long> entry: this.entries) {
            if(range.contains(entry.getValue())) {
                counter = counter + 1;
                this.rangeMap.put(range, counter);
            }
        }
        // if no file is inside this range add the entry with 0 as value
        if(counter == 0) {
            this.rangeMap.put(range, 0);
        }
    }
}
