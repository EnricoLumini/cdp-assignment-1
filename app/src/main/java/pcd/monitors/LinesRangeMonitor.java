package pcd.monitors;

import pcd.utils.Range;
import java.util.HashMap;
import java.util.Map;

public class LinesRangeMonitor {

    private final Map<Range, Integer> rangeMap;

    public LinesRangeMonitor(int ni, int maxl) {
        this.rangeMap = new HashMap<>();
        initMap(ni, maxl);
    }

    public synchronized void incRangeCount(Long lines) {
        for(Range range: this.rangeMap.keySet()) {
            if (range.contains(lines)) {
                this.rangeMap.put(range, this.rangeMap.get(range) + 1);
                break;
            }
        }
    }

    public synchronized void setRanges(int ni, int maxl) {
        this.initMap(ni, maxl);
    }

    public synchronized Map<Range, Integer> getRangeMap() {
        return this.rangeMap;
    }

    private void initMap(int ni, int maxl) {
        this.rangeMap.clear();
        long rangeDimension = maxl/ni;
        for(int i = 0; i < ni; i++) {
            this.rangeMap.put(Range.between(i*rangeDimension, rangeDimension*(i+1)-1), 0);
        }
        // add range [maxl, infinite]
        this.rangeMap.put(Range.between(maxl, -1), 0);
    }
}
