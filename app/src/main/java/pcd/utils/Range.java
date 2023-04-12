package pcd.utils;

public class Range {

    private final long low;
    private final long high;

    private Range(long low, long high) {
        this.low = low;
        this.high = high;
    }

    public static Range between(long low, long high) {
        return new Range(low, high);
    }

    public boolean contains(long n) {
        return this.high > -1 ? (n >= this.low && n <= this.high) : n >= this.low;
    }

    public long getLow() {
        return this.low;
    }

    @Override
    public String toString() {
        return this.high > -1 ? "[" + low + "," + high + "]" : ">" + low;
    }
}
