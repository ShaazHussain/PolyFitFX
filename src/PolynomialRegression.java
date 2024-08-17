import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class PolynomialRegression {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<double[]> dataPoints = new ArrayList<>();

        // Read the number of data points
        System.out.print("Enter the number of data points: ");
        int numPoints = scanner.nextInt();

        // Input data points
        for (int i = 0; i < numPoints; i++) {
            System.out.printf("Enter x and y for point %d (separated by space): ", i + 1);
            double x = scanner.nextDouble();
            double y = scanner.nextDouble();
            dataPoints.add(new double[]{x, y});
        }

        // Number of folds for cross-validation
        System.out.print("Enter the number of folds for cross-validation (e.g., 5): ");
        int k = scanner.nextInt();

        // Ensure k is valid
        if (k > numPoints) {
            System.out.println("Warning: The number of folds cannot exceed the number of data points.");
            k = numPoints; // Set k equal to the number of data points
        } else if (k <= 1) {
            System.out.println("Warning: The number of folds should be at least 2. Setting k to 2.");
            k = 2;
        }

        // Test different polynomial degrees and find the best one based on cross-validated R²
        int bestDegree = 1;
        double bestRSquared = -Double.MAX_VALUE;
        PolynomialFunction bestPolynomial = null;

        for (int degree = 1; degree <= 5; degree++) {
            double rSquared = crossValidate(degree, dataPoints, k);
            System.out.printf("Degree %d: Cross-Validated R² = %.4f%n", degree, rSquared);

            if (rSquared > bestRSquared && rSquared != Double.NEGATIVE_INFINITY) {
                bestRSquared = rSquared;
                bestDegree = degree;
                bestPolynomial = fitPolynomial(degree, dataPoints);
            }
        }

        // Check if a valid polynomial was found
        if (bestPolynomial != null) {
            // Print the best polynomial equation
            System.out.println("\nBest fitted polynomial equation:");
            for (int i = bestPolynomial.getCoefficients().length - 1; i >= 0; i--) {
                if (i == bestPolynomial.getCoefficients().length - 1) {
                    System.out.printf("%.2f", bestPolynomial.getCoefficients()[i]);
                } else {
                    System.out.printf(" + %.2fx^%d", bestPolynomial.getCoefficients()[i], i);
                }
            }
            System.out.println();

            // Print the fitted polynomial values for the input points
            System.out.println("Fitted values:");
            for (double[] point : dataPoints) {
                double x = point[0];
                double y = bestPolynomial.value(x);
                System.out.printf("x = %.2f, Predicted y = %.2f%n", x, y);
            }
        } else {
            System.out.println("No valid polynomial could be fitted to the data.");
        }

        scanner.close();
    }

    private static PolynomialFunction fitPolynomial(int degree, List<double[]> dataPoints) {
        WeightedObservedPoints points = new WeightedObservedPoints();
        for (double[] point : dataPoints) {
            points.add(point[0], point[1]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        double[] coefficients = fitter.fit(points.toList());
        return new PolynomialFunction(coefficients);
    }

    private static double crossValidate(int degree, List<double[]> dataPoints, int k) {
        int foldSize = dataPoints.size() / k;
        double totalRSquared = 0;

        for (int i = 0; i < k; i++) {
            List<double[]> trainingSet = new ArrayList<>();
            List<double[]> validationSet = new ArrayList<>();

            for (int j = 0; j < dataPoints.size(); j++) {
                if (j / foldSize == i) {
                    validationSet.add(dataPoints.get(j));
                } else {
                    trainingSet.add(dataPoints.get(j));
                }
            }

            if (validationSet.isEmpty()) continue;

            PolynomialFunction polynomial = fitPolynomial(degree, trainingSet);
            double rSquared = calculateRSquared(polynomial, validationSet);

            // If R² is invalid, skip this fold
            if (Double.isFinite(rSquared)) {
                totalRSquared += rSquared;
            }
        }

        // Return average R² over folds
        return k > 0 ? totalRSquared / k : Double.NEGATIVE_INFINITY;
    }

    private static double calculateRSquared(PolynomialFunction polynomial, List<double[]> dataPoints) {
        double ssTot = 0;
        double ssRes = 0;
        double meanY = dataPoints.stream().mapToDouble(p -> p[1]).average().orElse(0);

        for (double[] point : dataPoints) {
            double x = point[0];
            double y = point[1];
            double yPred = polynomial.value(x);
            ssRes += Math.pow(y - yPred, 2);
            ssTot += Math.pow(y - meanY, 2);
        }

        return ssTot > 0 ? 1 - (ssRes / ssTot) : Double.NEGATIVE_INFINITY;
    }
}