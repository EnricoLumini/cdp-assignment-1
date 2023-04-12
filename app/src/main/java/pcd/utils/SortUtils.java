package pcd.utils;

import java.util.*;

public class SortUtils {

    public static Map<Range, Integer> sortMapByRange(Map<Range, Integer> data) {
        Comparator<Map.Entry<Range, Integer>> rangeComparator = (o1, o2) -> {
            long l1 = o1.getKey().getLow();
            long l2 = o2.getKey().getLow();
            return Long.compare(l1, l2);
        };
        List<Map.Entry<Range, Integer>> listOfEntries = new ArrayList<>(data.entrySet());
        listOfEntries.sort(rangeComparator);
        // Recreate map in sorted order
        Map<Range, Integer> retData = new LinkedHashMap<>();
        for(Map.Entry<Range, Integer> entry: listOfEntries) {
            retData.put(entry.getKey(), entry.getValue());
        }
        return retData;
    }
}
