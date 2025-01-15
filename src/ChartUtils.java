import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

public class ChartUtils {

    /**
     * Plots the fitted polynomial curve and confidence intervals on the given chart.
     */
    public static void plotFittedCurveWithConfidence(
            LineChart<Number, Number> chart,
            double[] coeffs,
            double minX,
            double maxX,
            double stdError,
            int points,
            String curveLabel
    ) {
        XYChart.Series<Number, Number> fitSeries = new XYChart.Series<>();
        fitSeries.setName(curveLabel != null ? curveLabel : "Fitted Curve");

        XYChart.Series<Number, Number> upperBand = new XYChart.Series<>();
        XYChart.Series<Number, Number> lowerBand = new XYChart.Series<>();
        upperBand.setName("Confidence Upper");
        lowerBand.setName("Confidence Lower");

        double step = (maxX - minX) / (points - 1);

        for (int i = 0; i < points; i++) {
            double x = minX + i * step;
            double y = 0;
            for (int j = 0; j < coeffs.length; j++) {
                y += coeffs[j] * Math.pow(x, j + 1);
            }
            fitSeries.getData().add(new XYChart.Data<>(x, y));
            upperBand.getData().add(new XYChart.Data<>(x, y + 2 * stdError));
            lowerBand.getData().add(new XYChart.Data<>(x, y - 2 * stdError));
        }

        chart.getData().add(fitSeries);
        chart.getData().add(upperBand);
        chart.getData().add(lowerBand);

        if (upperBand.getNode() != null)
            upperBand.getNode().setStyle("-fx-stroke: #90caf9; -fx-stroke-dash-array: 2 4;");
        if (lowerBand.getNode() != null)
            lowerBand.getNode().setStyle("-fx-stroke: #90caf9; -fx-stroke-dash-array: 2 4;");
    }

    /**
     * Plots residuals vs. fitted values.
     */
    public static void plotResiduals(LineChart<Number, Number> chart, double[] fitted, double[] residuals) {
        XYChart.Series<Number, Number> residualSeries = new XYChart.Series<>();
        residualSeries.setName("Residuals");
        for (int i = 0; i < fitted.length; i++) {
            residualSeries.getData().add(new XYChart.Data<>(fitted[i], residuals[i]));
        }
        chart.getData().add(residualSeries);
    }

    /**
     * Utility to clear all series from a chart.
     */
    public static void clearChart(LineChart<?, ?> chart) {
        chart.getData().clear();
    }
}
