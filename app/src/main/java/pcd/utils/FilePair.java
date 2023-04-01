package pcd.utils;

public class FilePair<X,Y> {

    private final X filePath;
    private final Y lines;

    public FilePair(X x, Y y) {
        super();
        this.filePath = x;
        this.lines = y;
    }

    public X getFilePath() {
        return filePath;
    }

    public Y getLines() {
        return lines;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result + ((lines == null) ? 0 : lines.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FilePair other = (FilePair) obj;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        if (lines == null) {
            if (other.lines != null)
                return false;
        } else if (!lines.equals(other.lines))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Pair [x=" + filePath + ", y=" + lines + "]";
    }
}
