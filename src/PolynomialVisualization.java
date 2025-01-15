import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PolynomialVisualization extends Application {

    private ArrayList<Double> xData;
    private ArrayList<Double> yData;
    private int maxDegree;

    @Override
    public void start(Stage stage) {
        collectDataPoints();
        handleDegreeSelection(stage);
    }

    private void collectDataPoints() {
        int n = getPositiveInteger("Data Points", "Enter number of data points (at least 2):", 2);
        xData = new ArrayList<>();
        yData = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            double[] point = getDataPoint(i + 1);
            xData.add(point[0]);
            yData.add(point[1]);
        }

        maxDegree = Math.min(xData.size() - 1, 10);
    }

    private void handleDegreeSelection(Stage stage) {
        List<String> choices = List.of("Optimal Degree", "Specify Degree");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Optimal Degree", choices);
        dialog.setTitle("Degree Selection");
        dialog.setHeaderText("Choose degree selection method:");
        styleDialog(dialog.getDialogPane(), 400, 200);
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(choice -> {
            if (choice.equals("Optimal Degree")) {
                int degree = findBestDegree();
                showCoefficients(degree);
                plotPolynomial(stage, degree);
            } else {
                int degree = getManualDegree();
                if (degree != -1) {
                    showCoefficients(degree);
                    plotPolynomial(stage, degree);
                }
            }
        });
    }

    private int getManualDegree() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Manual Degree");
        dialog.setHeaderText("Enter polynomial degree (1-" + maxDegree + "):");
        styleDialog(dialog.getDialogPane(), 400, 200);

        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) return -1;

            try {
                int degree = Integer.parseInt(result.get());
                if (degree >= 1 && degree <= maxDegree) return degree;
                showError("Degree must be between 1 and " + maxDegree);
            } catch (NumberFormatException e) {
                showError("Invalid number format");
            }
        }
    }

    private int findBestDegree() {
        int bestDegree = 1;
        double minError = Double.MAX_VALUE;

        for (int degree = 1; degree <= maxDegree; degree++) {
            double error = crossValidate(degree);
            if (error < minError) {
                minError = error;
                bestDegree = degree;
            }
        }
        return bestDegree;
    }

    private double crossValidate(int degree) {
        int k = xData.size() < 10 ? xData.size() : 5;
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < xData.size(); i++) indices.add(i);
        Collections.shuffle(indices);

        double totalError = 0;
        int foldSize = (int) Math.ceil((double) xData.size() / k);

        for (int fold = 0; fold < k; fold++) {
            ArrayList<Double> trainX = new ArrayList<>();
            ArrayList<Double> trainY = new ArrayList<>();
            ArrayList<Double> validX = new ArrayList<>();
            ArrayList<Double> validY = new ArrayList<>();

            for (int i = 0; i < xData.size(); i++) {
                if (i >= fold * foldSize && i < (fold + 1) * foldSize) {
                    validX.add(xData.get(indices.get(i)));
                    validY.add(yData.get(indices.get(i)));
                } else {
                    trainX.add(xData.get(indices.get(i)));
                    trainY.add(yData.get(indices.get(i)));
                }
            }

            if (validX.isEmpty()) continue;

            double[] coeffs = fitPolynomial(degree, trainX, trainY);
            double error = calculateRMSE(coeffs, validX, validY);
            totalError += error;
        }
        return totalError / k;
    }

    private double[] fitPolynomial(int degree, ArrayList<Double> trainX, ArrayList<Double> trainY) {
        double meanX = calculateMean(trainX);
        double stdX = calculateStdDev(trainX, meanX);
        if (stdX == 0) stdX = 1;

        int n = trainX.size();
        double[][] xMatrix = new double[n][degree + 1];
        double[] yVector = new double[n];

        for (int i = 0; i < n; i++) {
            double normalizedX = (trainX.get(i) - meanX) / stdX;
            for (int j = 0; j <= degree; j++) {
                xMatrix[i][j] = Math.pow(normalizedX, j);
            }
            yVector[i] = trainY.get(i);
        }

        try {
            RealMatrix matrix = MatrixUtils.createRealMatrix(xMatrix);
            RealMatrix xtx = matrix.transpose().multiply(matrix);
            RealMatrix ridge = MatrixUtils.createRealIdentityMatrix(degree + 1).scalarMultiply(1e-6);
            RealVector xty = matrix.transpose().operate(MatrixUtils.createRealVector(yVector));

            DecompositionSolver solver = new QRDecomposition(xtx.add(ridge)).getSolver();
            return solver.solve(xty).toArray();
        } catch (SingularMatrixException e) {
            return new double[degree + 1];
        }
    }

    private void showCoefficients(int degree) {
        double[] coeffs = fitPolynomial(degree, xData, yData);
        double meanX = calculateMean(xData);
        double stdX = calculateStdDev(xData, meanX);

        StringBuilder equation = new StringBuilder("y = ");
        equation.append(String.format("%.4f", coeffs[0]));

        for (int i = 1; i <= degree; i++) {
            String term = String.format(" + %.4f", coeffs[i]);
            if (i == 1) {
                term += String.format(" · (x - %.2f)/%.2f", meanX, stdX);
            } else {
                term += String.format(" · [(x - %.2f)/%.2f]%s", meanX, stdX, toSuperscript(i));
            }
            equation.append(term);
        }

        // Create a scrollable TextArea for the equation
        TextArea textArea = new TextArea(equation.toString());
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-size: 16px; -fx-font-family: 'Consolas';");
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(200);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Polynomial Equation");
        dialog.setHeaderText("Degree " + degree + " Polynomial");
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(300);
        dialog.showAndWait();
    }


    private String toSuperscript(int number) {
        String[] superscripts = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
        StringBuilder result = new StringBuilder();
        for (char c : String.valueOf(number).toCharArray()) {
            int digit = Character.getNumericValue(c);
            result.append(superscripts[digit]);
        }
        return result.toString();
    }

    private void plotPolynomial(Stage stage, int degree) {
        double[] coeffs = fitPolynomial(degree, xData, yData);
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Polynomial Regression (Degree " + degree + ")");

        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data Points");
        for (int i = 0; i < xData.size(); i++) {
            dataSeries.getData().add(new XYChart.Data<>(xData.get(i), yData.get(i)));
        }

        XYChart.Series<Number, Number> curveSeries = new XYChart.Series<>();
        curveSeries.setName("Fitted Curve");
        double minX = Collections.min(xData);
        double maxX = Collections.max(xData);
        double meanX = calculateMean(xData);
        double stdX = calculateStdDev(xData, meanX);

        for (double x = minX; x <= maxX; x += (maxX - minX)/100) {
            double normalizedX = (x - meanX)/stdX;
            double y = 0;
            for (int i = 0; i <= degree; i++) {
                y += coeffs[i] * Math.pow(normalizedX, i);
            }
            curveSeries.getData().add(new XYChart.Data<>(x, y));
        }

        chart.getData().addAll(dataSeries, curveSeries);

        // Hide the line for the original data points
        chart.applyCss(); // Ensure nodes are created
        if (dataSeries.getNode() != null) {
            dataSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: transparent;");
        }
        // Make data points larger and a distinct color (e.g., red)
        for (XYChart.Data<Number, Number> data : dataSeries.getData()) {
            data.getNode().setStyle(
                    "-fx-background-color: #ff0000, white;" +  // red border, white fill
                            "-fx-background-insets: 0, 2;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-padding: 6px;"
            );
        }

        stage.setScene(new Scene(chart, 800, 600));
        stage.show();
    }

    // Helper Methods
    private int getPositiveInteger(String title, String prompt, int minValue) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(prompt);
            styleDialog(dialog.getDialogPane(), 400, 200);
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) System.exit(0);
            try {
                int value = Integer.parseInt(result.get().trim());
                if (value >= minValue) return value;
                showError("Please enter an integer ≥ " + minValue + ".");
            } catch (NumberFormatException e) {
                showError("Invalid number format.");
            }
        }
    }

    private double[] getDataPoint(int pointNumber) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Data Point " + pointNumber);
            dialog.setHeaderText("Enter x and y (space-separated):");
            styleDialog(dialog.getDialogPane(), 400, 200);
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) System.exit(0);
            String[] parts = result.get().trim().split("\\s+");
            if (parts.length != 2) {
                showError("Please enter exactly two numbers.");
                continue;
            }
            try {
                return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
            } catch (NumberFormatException e) {
                showError("Invalid number format.");
            }
        }
    }

    private double calculateMean(ArrayList<Double> data) {
        return data.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double calculateStdDev(ArrayList<Double> data, double mean) {
        double variance = data.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    private double calculateRMSE(double[] coefficients, ArrayList<Double> validX, ArrayList<Double> validY) {
        double sumSquaredError = 0.0;
        double meanX = calculateMean(validX);
        double stdX = calculateStdDev(validX, meanX);
        if (stdX == 0) stdX = 1;

        for (int i = 0; i < validX.size(); i++) {
            double x = validX.get(i);
            double normalizedX = (x - meanX) / stdX;
            double predicted = 0;
            for (int j = 0; j < coefficients.length; j++) {
                predicted += coefficients[j] * Math.pow(normalizedX, j);
            }
            sumSquaredError += Math.pow(predicted - validY.get(i), 2);
        }
        return Math.sqrt(sumSquaredError / validX.size());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        styleDialog(alert.getDialogPane(), 400, 200);
        alert.showAndWait();
    }

    private void styleDialog(DialogPane pane, int width, int height) {
        pane.setMinHeight(Region.USE_PREF_SIZE);
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
        pane.setStyle("-fx-font-size: 16px; -fx-background-color: #f0f8ff;");
        pane.setPadding(new Insets(20));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
