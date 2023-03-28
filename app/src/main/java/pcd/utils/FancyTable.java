package pcd.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FancyTable {

    private static final String FIRST_COL_LABEL = "File name";
    private static final String SECOND_COL_LABEL = "# of lines";

    private final List<Map.Entry<String, Long>> longestFiles;

    public FancyTable(int N, List<Map.Entry<String, Long>> listOfEntries) {
        this.longestFiles = listOfEntries.subList(0, N);
    }

    public void draw() {
        int longestLabelLength = longestFiles.stream().map(Map.Entry::getKey).max(Comparator.comparingInt(String::length)).get().length();
        int longestValueLength = longestFiles.stream().map(e -> e.getValue().toString()).max(Comparator.comparingInt(String::length)).get().length();
        longestLabelLength = Math.max(longestLabelLength, FIRST_COL_LABEL.length());
        longestValueLength = Math.max(longestValueLength, SECOND_COL_LABEL.length());

        // Display table entries
        String firstColSpaces = this.getSpaces((longestLabelLength-FIRST_COL_LABEL.length())/2);
        String secondColSpaces = this.getSpaces((longestValueLength-SECOND_COL_LABEL.length())/2);
        String tableEntriesLines = '+' + "-".repeat(longestLabelLength) + '+' + "-".repeat(longestValueLength) + '+';
        String firstColRow = String.format("|%s%s%s", firstColSpaces, FIRST_COL_LABEL, firstColSpaces);
        String secondColRow = String.format("|%s%s%s|", secondColSpaces, SECOND_COL_LABEL, secondColSpaces);
        System.out.println(tableEntriesLines);
        System.out.println(firstColRow + secondColRow);
        System.out.println(tableEntriesLines);

        // Display rows
        for(Map.Entry<String, Long> entry: longestFiles) {
            String fileName = entry.getKey();
            Long value = entry.getValue();
            String spacesToLabelBar = this.getSpaces(longestLabelLength - fileName.length());
            String spacesToValueBar = this.getSpaces(longestValueLength - value.toString().length());
            System.out.printf("|%s%s|%s%s|%n", fileName, spacesToLabelBar, value, spacesToValueBar);
        }

        // Close table
        System.out.println(tableEntriesLines);
    }

    private String getSpaces(int count) {
        return " ".repeat(count);
    }
}
