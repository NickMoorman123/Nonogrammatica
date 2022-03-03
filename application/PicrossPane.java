package application;

import javafx.scene.layout.Pane;

public class PicrossPane extends Pane {
	private int col;
	private int row;
	private boolean filled;
	
	public PicrossPane(int col, int row) {
		this.col = col;
		this.row = row;
		getStyleClass().add("cell");
	}
	
	public int getCol() {
		return col;
	}
	
	public int getRow() {
		return row;
	}
	
	//need to remove before adding so that css tags are not added multiple times
	public void setFilled() {
		getStyleClass().remove("clear");
		getStyleClass().remove("filled");
		getStyleClass().add("filled");
		filled = true;
	}
	
	public void clear() {
		getStyleClass().remove("filled");
		getStyleClass().remove("clear");
		getStyleClass().add("clear");
		filled = false;
	}
	
	public boolean filled() {
		return filled;
	}
	
	public void setUnknown() {
		getStyleClass().add("unknown");
	}
}