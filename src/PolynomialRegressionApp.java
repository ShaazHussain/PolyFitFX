import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.io.*;
import java.util.*;

public class PolynomialRegressionApp extends Application {

    private final ObservableList<DataPoint> data = FXCollections.observableArrayList();
    private TableView<DataPoint> tableView;
    private LineChart<Number, Number> lineChart;
    private TextField degreeField;

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

        // Chart setup
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X");
        yAxis.setLabel("Y");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Polynomial Regression");

        // Controls
        degreeField = new TextField("2");
        degreeField.setPrefWidth(40);
        Button fitButton = new Button("Fit Polynomial");
        fitButton.setOnAction(e -> fitAndPlot());

        HBox controls = new HBox(10, new Label("Degree:"), degreeField, fitButton);
        controls.setPadding(new Insets(10));

        // Add Point controls
        TextField xField = new TextField();
        xField.setPromptText("X");
        TextField yField = new TextField();
        yField.setPromptText("Y");
        Button addPointButton = new Button("Add Point");
        addPointButton.setOnAction(e -> {
            try {
                double x = Double.parseDouble(xField.getText().trim());
                double y = Double.parseDouble(yField.getText().trim());
                data.add(new DataPoint(x, y));
                xField.clear(); yField.clear();
                fitAndPlot();
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter valid numbers for X and Y.");
            }
        });

        // Remove Point controls
        Button removePointButton = new Button("Remove Selected");
        removePointButton.setOnAction(e -> {
            DataPoint selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                data.remove(selected);
                fitAndPlot();
            } else {
                showAlert("No Selection", "Please select a data point to remove.");
            }
        });

        HBox addRemoveBox = new HBox(5, xField, yField, addPointButton, removePointButton);
        addRemoveBox.setPadding(new Insets(10));

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem importItem = new MenuItem("Import CSV");
        MenuItem exportItem = new MenuItem("Export CSV");
        importItem.setOnAction(e -> importCSV(primaryStage));
        exportItem.setOnAction(e -> exportCSV(primaryStage));
        fileMenu.getItems().addAll(importItem, exportItem);
        menuBar.getMenus().add(fileMenu);

        // Layout
        VBox vbox = new VBox(menuBar, controls, addRemoveBox, new SplitPane(tableView, lineChart));
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Polynomial Regression Tool");
        primaryStage.show();
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

    // CSV Export
    private void exportCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (DataPoint dp : data) {
                    bw.write(dp.getX() + "," + dp.getY());
                    bw.newLine();
                }
            } catch (Exception ex) {
                showAlert("Error exporting CSV", ex.getMessage());
            }
        }
    }

    // Polynomial fit and plot
    private void fitAndPlot() {
        lineChart.getData().clear();
        if (data.isEmpty()) return;

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
        try {
            coeffs = regression.estimateRegressionParameters();
        } catch (Exception ex) {
            showAlert("Regression Error", "Could not fit polynomial: " + ex.getMessage());
            return;
        }

        // Plot fitted curve
        XYChart.Series<Number, Number> fitSeries = new XYChart.Series<>();
        fitSeries.setName("Fitted Polynomial");
        double minX = Arrays.stream(xArr).min().orElse(0);
        double maxX = Arrays.stream(xArr).max().orElse(1);
        int points = 100;
        double step = (maxX - minX) / (points - 1);
        for (int i = 0; i < points; i++) {
            double x = minX + i * step;
            double y = 0;
            for (int j = 0; j < coeffs.length; j++) {
                y += coeffs[j] * Math.pow(x, j + 1);
            }
            fitSeries.getData().add(new XYChart.Data<>(x, y));
        }
        lineChart.getData().add(fitSeries);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
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
        public void setX(double x) { this.x.set(x); }
        public DoubleProperty xProperty() { return x; }

        public double getY() { return y.get(); }
        public void setY(double y) { this.y.set(y); }
        public DoubleProperty yProperty() { return y; }
    }
}
