
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

// This class exists so we don't track parallel arrays
public class InputAndError {
    public TextField Input;
    public Label Error;

    public InputAndError() {
        Input = new TextField();
        Error = new Label();
    }
}
