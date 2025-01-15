import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public class UIUtils {

    /**
     * Shows an error alert dialog.
     */
    public static void showError(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog.
     */
    public static void showInfo(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and returns true if user clicks OK.
     */
    public static boolean showConfirmation(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner != null) alert.initOwner(owner);
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }
}
