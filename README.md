                                                        POLYFITFX
                                                                 
PolyFitFX is a Java-based application for performing polynomial regression and visualizing data. It allows users to input data points, fit a polynomial curve of a specified degree, and display the results in an interactive graph using JavaFX.

Features:

(i) Interactive Data Input: Takes manual input of data points directly through the console.

(ii) Polynomial Regression: Fits a polynomial curve to the data using the least squares method.

(iii) Real-Time Visualization: Displays the fitted polynomial curve alongside the original data points.

(iv) Custom Polynomial Degree: Allows selection of polynomial degree which can be obtained through regression to avoid overfitting or underfitting.

(v) Manual Matrix Operations: Implements basic matrix operations (transpose, multiplication, Gaussian elimination) without relying on external libraries.

Prerequisites:

Java Development Kit - JDK 8

JavaFX - JavaFX libraries need to be included in the project setup.

Project Structure

PolynomialRegression.java: Takes data points and the required type of cross-validation as input and gives the equation of the best fit curve of the given data points.

PolynomialVisualization.java: Handles user input, polynomial fitting, and JavaFX visualization of the curve in graphical form.

README.md: Project documentation for better understanding of the project.

ACKNOWLEDGEMENTS:

JavaFX: For providing a rich set of graphics and media APIs.

Apache Commons Math: For inspiration in implementing matrix operations manually.

