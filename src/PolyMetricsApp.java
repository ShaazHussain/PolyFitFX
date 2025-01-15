import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

public class PolyMetricsApp extends Application {
    private final ObservableList<DataPoint> data = FXCollections.observableArrayList();
    private TableView<DataPoint> tableView;
    private LineChart<Number, Number> lineChart;
    private LineChart<Number, Number> residualChart;
    private TextField degreeField;
    private Label metricsLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Table setup
        tableView = new TableView<>();
        TableColumn<DataPoint, Double> xCol = new TableColumn<>("X");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<DataPoint, Double> yCol = new TableColumn<>("Y");
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        tableView.getColumns().addAll(xCol, yCol);
        tableView.setItems(data);

        // Main regression chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X");
        yAxis.setLabel("Y");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Polynomial Regression");
        lineChart.setAnimated(false);

        // Residual plot chart
        NumberAxis rxAxis = new NumberAxis();
        NumberAxis ryAxis = new NumberAxis();
        rxAxis.setLabel("Fitted Value");
        ryAxis.setLabel("Residual");
        residualChart = new LineChart<>(rxAxis, ryAxis);
        residualChart.setTitle("Residual Plot");
        residualChart.setAnimated(false);

        // Controls
        degreeField = new TextField("2");
        degreeField.setPrefWidth(40);
        Button fitButton = new Button("Fit Polynomial");
        fitButton.setOnAction(e -> fitAndPlot());

        HBox controls = new HBox(10, new Label("Degree:"), degreeField, fitButton);
        controls.setPadding(new Insets(10));

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem importItem = new MenuItem("Import CSV");
        importItem.setOnAction(e -> importCSV(primaryStage));
        fileMenu.getItems().addAll(importItem);
        menuBar.getMenus().add(fileMenu);

        // Metrics label
        metricsLabel = new Label("Metrics will appear here.");
        metricsLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        // Layout
        VBox vbox = new VBox(menuBar, controls, metricsLabel, new SplitPane(tableView, lineChart, residualChart));
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        VBox.setVgrow(residualChart, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Polynomial Regression with Metrics and Residuals");
        primaryStage.show();

        // Enable zoom/pan for main chart
        enableZoomAndPan(lineChart);
        enableZoomAndPan(residualChart);
    }

    // CSV Import
    private void importCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                data.clear();
                while ((line = br.readLine()) != null) {
                    String[] parts = line.trim().split(",");
                    if (parts.length == 2) {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        data.add(new DataPoint(x, y));
                    }
                }
                fitAndPlot();
            } catch (Exception ex) {
                showAlert("Error importing CSV", ex.getMessage());
            }
        }
    }

    // Polynomial fit and plot with metrics and residuals
    private void fitAndPlot() {
        lineChart.getData().clear();
        residualChart.getData().clear();
        if (data.isEmpty()) {
            metricsLabel.setText("No data loaded.");
            return;
        }

        // Plot data points
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Data Points");
        for (DataPoint dp : data) {
            series.getData().add(new XYChart.Data<>(dp.getX(), dp.getY()));
        }
        lineChart.getData().add(series);

        // Fit polynomial
        int degree;
        try {
            degree = Integer.parseInt(degreeField.getText().trim());
            if (degree < 1) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert("Invalid Degree", "Please enter a positive integer for degree.");
            return;
        }

        double[] xArr = data.stream().mapToDouble(DataPoint::getX).toArray();
        double[] yArr = data.stream().mapToDouble(DataPoint::getY).toArray();

        double[][] xPoly = new double[xArr.length][degree];
        for (int i = 0; i < xArr.length; i++) {
            for (int j = 0; j < degree; j++) {
                xPoly[i][j] = Math.pow(xArr[i], j + 1);
            }
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.setNoIntercept(true);
        regression.newSampleData(yArr, xPoly);
        double[] coeffs;
        double[] yPred = new double[xArr.length];
        double[] residuals = new double[xArr.length];
        try {
            coeffs = regression.estimateRegressionParameters();
            for (int i = 0; i < xArr.length; i++) {
                yPred[i] = 0;
                for (int j = 0; j < coeffs.length; j++) {
                    yPred[i] += coeffs[j] * Math.pow(xArr[i], j + 1);
                }
                residuals[i] = yArr[i] - yPred[i];
            }
        } catch (Exception ex) {
            showAlert("Regression Error", "Could not fit polynomial: " + ex.getMessage());
            return;
        }

        // Compute metrics
        double r2 = computeR2(yArr, yPred);
        double rmse = computeRMSE(yArr, yPred);
        double mae = computeMAE(yArr, yPred);

        metricsLabel.setText(String.format("R² = %.4f   Root Mean Square Error = %.4f   Mean Absolute Error = %.4f", r2, rmse, mae));

        // Plot fitted curve and confidence band
        XYChart.Series<Number, Number> fitSeries = new XYChart.Series<>();
        fitSeries.setName("Fitted Polynomial");
        XYChart.Series<Number, Number> upperBand = new XYChart.Series<>();
        XYChart.Series<Number, Number> lowerBand = new XYChart.Series<>();
        fitSeries.setName("Fitted Curve");
        upperBand.setName("Confidence Upper");
        lowerBand.setName("Confidence Lower");

        double minX = Arrays.stream(xArr).min().orElse(0);
        double maxX = Arrays.stream(xArr).max().orElse(1);
        int points = 100;
        double step = (maxX - minX) / (points - 1);
        double residualStd = stddev(residuals);

        for (int i = 0; i < points; i++) {
            double x = minX + i * step;
            double y = 0;
            for (int j = 0; j < coeffs.length; j++) {
                y += coeffs[j] * Math.pow(x, j + 1);
            }
            fitSeries.getData().add(new XYChart.Data<>(x, y));
            upperBand.getData().add(new XYChart.Data<>(x, y + 2 * residualStd));
            lowerBand.getData().add(new XYChart.Data<>(x, y - 2 * residualStd));
        }

        lineChart.getData().add(fitSeries);
        lineChart.getData().add(upperBand);
        lineChart.getData().add(lowerBand);

        // Style confidence bands
        upperBand.getNode().setStyle("-fx-stroke: #90caf9; -fx-stroke-dash-array: 2 4;");
        lowerBand.getNode().setStyle("-fx-stroke: #90caf9; -fx-stroke-dash-array: 2 4;");

        // Residual plot
        XYChart.Series<Number, Number> residualSeries = new XYChart.Series<>();
        residualSeries.setName("Residuals");
        for (int i = 0; i < xArr.length; i++) {
            residualSeries.getData().add(new XYChart.Data<>(yPred[i], residuals[i]));
        }
        residualChart.getData().add(residualSeries);
    }

    private double computeR2(double[] actual, double[] predicted) {
        double mean = Arrays.stream(actual).average().orElse(0);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < actual.length; i++) {
            ssTot += Math.pow(actual[i] - mean, 2);
            ssRes += Math.pow(actual[i] - predicted[i], 2);
        }
        return 1 - ssRes / ssTot;
    }

    private double computeRMSE(double[] actual, double[] predicted) {
        double sum = 0;
        for (int i = 0; i < actual.length; i++) {
            sum += Math.pow(actual[i] - predicted[i], 2);
        }
        return Math.sqrt(sum / actual.length);
    }

    private double computeMAE(double[] actual, double[] predicted) {
        double sum = 0;
        for (int i = 0; i < actual.length; i++) {
            sum += Math.abs(actual[i] - predicted[i]);
        }
        return sum / actual.length;
    }

    private double stddev(double[] arr) {
        double mean = Arrays.stream(arr).average().orElse(0);
        double sum = 0;
        for (double v : arr) sum += Math.pow(v - mean, 2);
        return Math.sqrt(sum / arr.length);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Simple zoom and pan for charts
    private void enableZoomAndPan(LineChart<Number, Number> chart) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        chart.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mouseAnchor.set(new Point2D(e.getX(), e.getY()));
            }
        });
        chart.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY && mouseAnchor.get() != null) {
                double dx = e.getX() - mouseAnchor.get().getX();
                double dy = e.getY() - mouseAnchor.get().getY();
                chart.setTranslateX(chart.getTranslateX() + dx);
                chart.setTranslateY(chart.getTranslateY() + dy);
                mouseAnchor.set(new Point2D(e.getX(), e.getY()));
            }
        });
        chart.setOnScroll(e -> {
            double zoomFactor = (e.getDeltaY() > 0) ? 1.1 : 0.9;
            chart.setScaleX(chart.getScaleX() * zoomFactor);
            chart.setScaleY(chart.getScaleY() * zoomFactor);
        });
    }

    // DataPoint class
    public static class DataPoint {
        private final DoubleProperty x = new SimpleDoubleProperty();
        private final DoubleProperty y = new SimpleDoubleProperty();

        public DataPoint(double x, double y) {
            this.x.set(x);
            this.y.set(y);
        }
        public double getX() { return x.get(); }
        public DoubleProperty xProperty() { return x; }
        public double getY() { return y.get(); }
        public DoubleProperty yProperty() { return y; }
    }
}
