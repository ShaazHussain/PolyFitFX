import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Scanner;

public class PolynomialVisualization extends Application {

    @Override
    public void start(Stage stage) {
        Scanner scanner = new Scanner(System.in);

        // Get the number of data points
        System.out.print("Enter the number of data points: ");
        int n = scanner.nextInt();

        // Input data points
        ArrayList<Double> xData = new ArrayList<>();
        ArrayList<Double> yData = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.print("Enter x and y for point " + (i + 1) + " (separated by space): ");
            xData.add(scanner.nextDouble());
            yData.add(scanner.nextDouble());
        }

        // Enter the degree of the polynomial
        System.out.print("Enter the degree of the polynomial (obtained from regression): ");
        int degree = scanner.nextInt();

        // Fit polynomial
        double[] coefficients = fitPolynomial(degree, xData, yData);

        // Create X and Y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X");
        yAxis.setLabel("Y");

        // Create the line chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Polynomial Regression");

        // Add scatter plot for original data
        XYChart.Series<Number, Number> originalSeries = new XYChart.Series<>();
        originalSeries.setName("Original Data");
        for (int i = 0; i < xData.size(); i++) {
            originalSeries.getData().add(new XYChart.Data<>(xData.get(i), yData.get(i)));
        }

        // Add fitted polynomial curve
        XYChart.Series<Number, Number> fittedSeries = new XYChart.Series<>();
        fittedSeries.setName("Fitted Curve");
        double minX = xData.get(0);
        double maxX = xData.get(xData.size() - 1);
        for (double x = minX; x <= maxX; x += (maxX - minX) / 100) {
            double y = 0;
            for (int i = 0; i < coefficients.length; i++) {
                y += coefficients[i] * Math.pow(x, i);
            }
            fittedSeries.getData().add(new XYChart.Data<>(x, y));
        }

        // Add series to the chart
        lineChart.getData().addAll(originalSeries, fittedSeries);

        // Set the scene and stage
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private double[] fitPolynomial(int degree, ArrayList<Double> xData, ArrayList<Double> yData) {
        int n = xData.size();
        double[][] xMatrix = new double[n][degree + 1];
        double[] yVector = new double[n];

        // Construct the X matrix and Y vector
        for (int i = 0; i < n; i++) {
            yVector[i] = yData.get(i);
            for (int j = 0; j <= degree; j++) {
                xMatrix[i][j] = Math.pow(xData.get(i), j);
            }
        }

        // Transpose X matrix
        double[][] xMatrixT = transposeMatrix(xMatrix);

        // Multiply X^T with X
        double[][] xTx = multiplyMatrices(xMatrixT, xMatrix);

        // Multiply X^T with Y
        double[] xTy = multiplyMatrixVector(xMatrixT, yVector);

        // Solve the system (X^T * X) * coefficients = X^T * Y using Gaussian elimination
        return gaussianElimination(xTx, xTy);
    }

    private double[][] transposeMatrix(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposedMatrix = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }
        return transposedMatrix;
    }

    private double[][] multiplyMatrices(double[][] matrixA, double[][] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;
        double[][] result = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return result;
    }

    private double[] multiplyMatrixVector(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    private double[] gaussianElimination(double[][] matrix, double[] vector) {
        int n = vector.length;
        double[] result = new double[n];

        // Perform Gaussian elimination
        for (int i = 0; i < n; i++) {
            // Make the diagonal contain all 1's
            for (int j = i + 1; j < n; j++) {
                double factor = matrix[j][i] / matrix[i][i];
                for (int k = 0; k < n; k++) {
                    matrix[j][k] -= factor * matrix[i][k];
                }
                vector[j] -= factor * vector[i];
            }
        }

        // Perform back substitution
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += matrix[i][j] * result[j];
            }
            result[i] = (vector[i] - sum) / matrix[i][i];
        }

        return result;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
