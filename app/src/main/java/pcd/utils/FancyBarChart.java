package pcd.utils;

import java.util.*;

public class FancyBarChart {

    private final int numberOfFiles;
    private final int maxl;
    private final Map<Range, Integer> data;

    private final int increment;
    private final int longestLabelLength;
    private final int longestValueLength;

    public FancyBarChart(int maxl, int numberOfFiles, Map<Range, Integer> data) {
        this.maxl = maxl;
        this.numberOfFiles = numberOfFiles;
        this.data = data;
        int maxValue = Collections.max(data.values());
        this.increment = maxValue / 25;
        this.longestLabelLength = data.keySet().stream().map(Object::toString).max(Comparator.comparingInt(String::length)).get().length();
        this.longestValueLength = Integer.toString(maxValue).length();
    }

    public void draw() {
        Map<Range, Integer> sortedData = this.sortMapByRange();
        for(Map.Entry<Range, Integer> entry: sortedData.entrySet()) {
            System.out.println(this.getBar(entry.getValue(), entry.getKey().toString()));
        }
        int rangedFiles = sortedData.values().stream().reduce(0, Integer::sum);
        int numberOfFilesOverRanges = this.numberOfFiles - rangedFiles;
        if(numberOfFilesOverRanges > 0) {
            System.out.println(this.getBar(numberOfFilesOverRanges, ">" + this.maxl));
        }
    }

    private String getBar(int count, String label) {
        int bar_chuncks = (count * 8 / increment) / 8;
        int remainder = (count * 8 / increment) % 8;

        String bar = "█".repeat(bar_chuncks);

        if(remainder > 0) {
            bar = bar + (char) ((int) '█' + (8 - remainder));
        }
        return String.format("%" + longestLabelLength + "s", label) + " |    " + String.format("%" + longestValueLength + "d", count) + " " + bar;
    }


    private Map<Range, Integer> sortMapByRange() {
        Comparator<Map.Entry<Range, Integer>> rangeComparator = (o1, o2) -> {
            long l1 = o1.getKey().getLow();
            long l2 = o2.getKey().getLow();
            return Long.compare(l1, l2);
        };
        List<Map.Entry<Range, Integer>> listOfEntries = new ArrayList<>(this.data.entrySet());
        listOfEntries.sort(rangeComparator);
        // Recreate map in sorted order
        Map<Range, Integer> data = new LinkedHashMap<>();
        for(Map.Entry<Range, Integer> entry: listOfEntries) {
            data.put(entry.getKey(), entry.getValue());
        }
        return data;
    }
}
