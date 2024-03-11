package Picross;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

class PicrossPane extends StackPane {
    private final int row;
    private final int col;
    private boolean filled;
    
    public PicrossPane(int row, int col) {
        this.row = row;
        this.col = col;
        
        if (col % 5 == 0) {
			getStyleClass().add("after-fifth-column");
		}
		if (row % 5 == 0) {
			getStyleClass().add("after-fifth-row");
		}
        
        getStyleClass().add("cell");
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setFilled() {
        if (filled) {
            return;
        }
        
        getStyleClass().add("filled");
        filled = true;
    }
    
    public void setCrossed() {
        Pane crossForward = new Pane();
        crossForward.getStyleClass().add("cross-forward-slash");
        crossForward.setPadding(new Insets(1,1,1,1));
        Pane crossBack = new Pane();
        crossBack.getStyleClass().add("cross-back-slash");
        crossBack.setPadding(new Insets(5,5,5,5));
        this.getChildren().addAll(crossForward, crossBack);
    }
    
    public void clear() {
        if (!filled) {
            return;
        }
        
        getStyleClass().remove("filled");
        filled = false;
    }
    
    public boolean filled() {
        return filled;
    }
    
    public void setCursor() {
        getStyleClass().add("cursor");
    }
    
    public void removeCursor() {
        getStyleClass().remove("cursor");
    }
}