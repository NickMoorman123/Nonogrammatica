package application;

import javafx.scene.layout.Pane;

public class PicrossPane extends Pane {
	private int col;
	private int row;
	
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
	
	public void setFilled() {
		clear();
		getStyleClass().add("filled");			
	}
	
	public void clear() {
		getStyleClass().remove("filled");
	}
}