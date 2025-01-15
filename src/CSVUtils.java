import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    /**
     * Reads a CSV file and returns a list of DataPoint objects.
     * Skips header if present and ignores malformed lines.
     */
    public static List<PolyMetricsApp.DataPoint> importCSV(File file, boolean hasHeader) throws IOException {
        List<PolyMetricsApp.DataPoint> dataPoints = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine && hasHeader) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.trim().split(",");
                if (parts.length != 2) continue;
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    dataPoints.add(new PolyMetricsApp.DataPoint(x, y));
                } catch (NumberFormatException nfe) {
                    // Ignore malformed lines
                }
            }
        }
        return dataPoints;
    }

    /**
     * Exports a list of DataPoint objects to a CSV file.
     * Optionally writes a header.
     */
    public static void exportCSV(File file, ObservableList<DataPoint> data, boolean writeHeader) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            if (writeHeader) {
                bw.write("x,y");
                bw.newLine();
            }
            for (DataPoint dp : data) {
                bw.write(dp.getX() + "," + dp.getY());
                bw.newLine();
            }
        }
    }
}
