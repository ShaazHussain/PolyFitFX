# POLYFITFX
                                                                 
This project is a Java-based desktop application built with JavaFX and Apache Commons Math for performing polynomial regression analysis. It provides an interactive user interface to import data, fit polynomial models, visualize results and evaluate model performance.

![Screenshot (101)](https://github.com/user-attachments/assets/a8d5d910-0f3f-4c60-9322-bb96482dd3c3)
![Screenshot (102)](https://github.com/user-attachments/assets/483b5c41-1090-4c40-8580-53ebb5ae96c2)
![Screenshot (104)](https://github.com/user-attachments/assets/f3a410ed-430b-4765-9c45-4a2852403176)

## Features
- Add and remove data points manually through the UI or import and export data points via CSV files.
- Select polynomial degree for regression fitting or use the most optimal degree caculated by the application.
- View regression curves, residual plots and confidence intervals.
- Calculate and display model evaluation metrics: R², RMSE, and MAE.
- Export regression results for advanced visualization in Power BI.
- Modular design with utility classes for CSV handling, chart updates, and UI dialogs.
- Comprehensive user manual and sample dataset included.

## Getting Started
1. Ensure Java 8 or higher and JavaFX are installed.
2. Build and run the application using your preferred IDE or command line.
3. Use the File menu to import data from CSV or add points manually.
4. Enter the desired polynomial degree and fit the model.
5. Visualize results and export regression data for Power BI if needed.

## Project Structure
- `ChartUtils.java`: Utility functions for chart creation and updates.
- `CSVUtils.java`: Robust CSV import/export handling.
- `DataPoint.java`: Data model representing a single data point.
- `PolyMetricsApp.java`: Application variant focusing on model metrics.
- `PolynomialRegressionApp.java`: Main application with full features.
- `PolynomialVisualizationApp.java`: Visualization-focused application variant.
- `UIUtils.java`: UI helper methods for dialogs and alerts.
- `SampleData.csv`: Example dataset for testing.
- `UserManual.pdf`: Detailed user guide.
- `RegressionReport.pbix`: Power BI report for advanced visualization.

## Usage
- Import your dataset or add points manually.
- Choose polynomial degree and fit the model.
- Analyze the regression curve and residuals.
- Export results for further analysis in Power BI.

## Acknowledgements
JavaFX: For providing a rich set of graphics and media APIs.
Apache Commons Math: For inspiration in implementing matrix operations manually.

