package pcd.utils;

import java.util.Comparator;
import java.util.List;

public class FancyTable {

    private static final String FIRST_COL_LABEL = "File name";
    private static final String SECOND_COL_LABEL = "# of lines";

    private final List<FilePair<String, Long>> data;

    public FancyTable(List<FilePair<String, Long>> data) {
        this.data = data;
    }

    public void draw() {
        int longestLabelLength = data.stream().map(FilePair::getFilePath).max(Comparator.comparingInt(String::length)).get().length();
        int longestValueLength = data.stream().map(e -> e.getLines().toString()).max(Comparator.comparingInt(String::length)).get().length();
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
        for(FilePair<String, Long> entry: data) {
            String fileName = entry.getFilePath();
            Long value = entry.getLines();
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
