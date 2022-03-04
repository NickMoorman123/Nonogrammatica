package application;

import java.awt.Desktop;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PicrossGrid extends GridPane {
	private PicrossPane[][] pointersToPanes;
	private PicrossText[] pointersToTexts;
	private PicrossPane current;
	private KeyCode state = KeyCode.SPACE;
	private KeyCode direction = KeyCode.SPACE;
	private boolean justMoved = false;
	private boolean unsavedChanges = false;
	private int numCols;
	private int numRows;
	private int mult;
	private int cellSize = 18;
	public PicrossGrid(int numCols, int numRows, boolean editing) {
		this.numCols = numCols;
		this.numRows = numRows;
		//some pointers will be null, for consistency in indexes
		pointersToPanes = new PicrossPane[numCols + 1][numRows + 1];
		pointersToTexts = new PicrossText[numCols + numRows];
		
		//make sure top left empty space has correct borders
		PicrossPane topLeft = new PicrossPane(0, 0);
		topLeft.getStyleClass().add("nums");
		add(topLeft, 0, 0);
		pointersToPanes[0][0] = topLeft;
		setVgrow(topLeft, Priority.ALWAYS);
		setHgrow(topLeft, Priority.ALWAYS);
		
		//settings for row and column labels, though
		//sizes will shortly be overwritten
		ColumnConstraints rowLabels = new ColumnConstraints();
		rowLabels.setHgrow(Priority.ALWAYS);
		rowLabels.setHalignment(HPos.RIGHT);
		getColumnConstraints().add(rowLabels);
		RowConstraints columnLabels = new RowConstraints();
		columnLabels.setVgrow(Priority.ALWAYS);
		columnLabels.setValignment(VPos.BOTTOM);
		getRowConstraints().add(columnLabels);
		
		//add column heights and numbers
		int[] startZero = {0};
		ColumnConstraints columnGrid = new ColumnConstraints(cellSize);
		for (int i = 1; i < numCols + 1; i++) {
			getColumnConstraints().add(columnGrid);
			PicrossText nums = new PicrossText(startZero, "\n");
			nums.setTextAlignment(TextAlignment.CENTER);
			pointersToTexts[i - 1] = nums;
			VBox labelCell = new VBox(nums);
			setVgrow(labelCell, Priority.ALWAYS);
			labelCell.setAlignment(Pos.BOTTOM_CENTER);
			
			//thick border every 5
			labelCell.getStyleClass().add("nums");
			if (i % 5 == 1) {
				labelCell.getStyleClass().add("after-fifth-column");
			}
			add(labelCell, i, 0);
		}
		
		//add row lengths and number labels
		RowConstraints rowGrid = new RowConstraints(cellSize);
		for (int i = 1; i < numRows + 1; i++) {
			getRowConstraints().add(rowGrid);
			PicrossText nums = new PicrossText(startZero, " ");
			pointersToTexts[numCols + i - 1] = nums;
			HBox labelCell = new HBox(nums);
			setHgrow(labelCell, Priority.ALWAYS);
			labelCell.setAlignment(Pos.CENTER_RIGHT);
			
			//thick border every 5
			labelCell.getStyleClass().add("nums");
			if (i % 5 == 1) {
				labelCell.getStyleClass().add("after-fifth-row");
			}
			
			add(labelCell, 0, i);
		}
		
		//add cells
		for (int c = 1; c <= numCols; c++) {
			for (int r = 1; r <= numRows; r++) {
				PicrossPane cell = new PicrossPane(c, r);
				
				//thick borders every 5
				if (c % 5 == 1) {
					cell.getStyleClass().add("after-fifth-column");
				}
				if (r % 5 == 1) {
					cell.getStyleClass().add("after-fifth-row");
				}
				
				//allow change of cursor by clicking
				if (editing) {
					cell.setOnMouseClicked(e -> {
						moveCursor(cell.getCol(), cell.getRow());
						cell.getParent().requestFocus();
					});
				}
				
				add(cell, c, r);
				pointersToPanes[c][r] = cell;
			}
		}
		
		//coloring and moving input handlers
		setOnKeyPressed(e -> {
			keyEntered(e.getCode());
			e.consume();
		});
		setOnKeyReleased(e -> {
			keyRemoved(e.getCode());
			e.consume();
		});
		
		resizeLabels();
		if (editing) {
			current = pointersToPanes[1][1];
			current.getStyleClass().add("cursor");
		}
	}
	
	//handler for movement and coloring
	final AnimationTimer timer = new AnimationTimer() {
		private long lastUpdate = 0;
		
		@Override
		public void handle(long time) {
			//immediately try to color
			//introduce a delay if just moved
			color();
			if (time - lastUpdate >= mult * 40_000_000) {
				move();
				if (justMoved) {
					mult = 6;
					justMoved = false;
				} else {
					mult = 1;
				}
				lastUpdate = time;
			}
		}
	};
	
	//color current cell based on state variable
	private void color() {
		if (state == KeyCode.C) {
			current.clear();
			updateLabelsCol(current.getCol());
			updateLabelsRow(current.getRow());
			resizeLabels();
		} else if (state == KeyCode.Z) {
			current.setFilled();
			updateLabelsCol(current.getCol());
			updateLabelsRow(current.getRow());
			resizeLabels();
		}
	}
	
	//change label of a column
	private void updateLabelsCol(int currentCol) {
		LinkedList<Integer> colNums = new LinkedList<Integer>();
		int tempBlock = 0;
		for (int r = 1; r <= numRows; r++) {
			if (pointersToPanes[currentCol][r].filled()) {
				tempBlock++;
			} else {
				if (tempBlock > 0) {
					colNums.add(tempBlock);
					tempBlock = 0;
				}
			}
			if (r == numRows && tempBlock > 0) {
				colNums.add(tempBlock);
				tempBlock = 0;
			}
		}
		int[] newColNums = colNums.stream().mapToInt(Integer::intValue).toArray();
		if (newColNums.length == 0) {
			int[] startZero = {0};
			pointersToTexts[currentCol - 1].updateText(startZero);
		} else {
			pointersToTexts[currentCol - 1].updateText(newColNums);
		}
	}
	
	//change label of a row
	private void updateLabelsRow(int currentRow) {
		LinkedList<Integer> rowNums = new LinkedList<Integer>();
		int tempBlock = 0;
		for (int c = 1; c <= numCols; c++) {
			if (pointersToPanes[c][currentRow].filled()) {
				tempBlock++;
			} else {
				if (tempBlock > 0) {
					rowNums.add(tempBlock);
					tempBlock = 0;
				}
			}
			if (c == numCols && tempBlock > 0) {
				rowNums.add(tempBlock);
				tempBlock = 0;
			}
		}
		int[] newRowNums = rowNums.stream().mapToInt(Integer::intValue).toArray();
		if (newRowNums.length == 0) {
			int[] startZero = {0};
			pointersToTexts[numCols + currentRow - 1].updateText(startZero);
		} else {
			pointersToTexts[numCols + currentRow - 1].updateText(newRowNums);
		}
	}
	
	//overwrite labels with puzzle labels when dislaying solver results
	public void overrideLabels(int[][][] labels) {
		int[][] colLabels = labels[0];
		int[][] rowLabels = labels[1];
		
		for (int c = 0; c < numCols; c++) {
			pointersToTexts[c].updateText(colLabels[c]);
		}
		for (int r = 0; r < numRows; r++) {
			pointersToTexts[numCols + r].updateText(rowLabels[r]);
		}
		
		resizeLabels();
	}
	
	//resize boxes holding PicrossTexts
	private void resizeLabels() {
		int newHeight = 0;
		for (int c = 0; c < numCols; c++) {
			int height = (int) pointersToTexts[c].getLayoutBounds().getHeight();
			if (height > newHeight) {
				newHeight = height;
			}
		}
		pointersToPanes[0][0].setMinHeight(newHeight + 2);
		pointersToPanes[0][0].setMaxHeight(newHeight + 2);
		for (int c = 0; c < numCols; c++) {
			((VBox) pointersToTexts[c].getParent()).setMinHeight(newHeight + 2);
			((VBox) pointersToTexts[c].getParent()).setMaxHeight(newHeight + 2);
		}
		
		int newWidth = 0;
		for (int r = 0; r < numRows; r++) {
			int width = (int) pointersToTexts[numCols + r].getLayoutBounds().getWidth();
			if (width > newWidth) {
				newWidth = width;
			}
		}
		pointersToPanes[0][0].setMinWidth(newWidth + 4);
		pointersToPanes[0][0].setMaxWidth(newWidth + 4);
		for (int r = 0; r < numRows; r++) {
			((HBox) pointersToTexts[numCols + r].getParent()).setMinWidth(newWidth + 4);
			((HBox) pointersToTexts[numCols + r].getParent()).setMaxWidth(newWidth + 4);
		}
	}
	
	//trigger move based on state and position
	private void move() {
		int currentCol = current.getCol();
		int currentRow = current.getRow();
		if (direction == KeyCode.RIGHT || direction == KeyCode.KP_RIGHT) {
			moveCursor(currentCol + 1, currentRow);
		} else if (direction == KeyCode.DOWN || direction == KeyCode.KP_DOWN) {
			moveCursor(currentCol, currentRow + 1);
		} else if (direction == KeyCode.LEFT || direction == KeyCode.KP_LEFT) {
			moveCursor(currentCol - 1, currentRow);
		} else if (direction == KeyCode.UP || direction == KeyCode.KP_UP) {
			moveCursor(currentCol, currentRow - 1);
		}
	}
	
	//change current cell
	private void moveCursor(int col, int row) {
		current.getStyleClass().remove("cursor");
		current = pointersToPanes[Math.max(1, Math.min(col, numCols))][Math.max(1, Math.min(row, numRows))];
		current.getStyleClass().add("cursor");
	}
	
	//set the state only if the state is clear or change direction
	public void keyEntered(KeyCode key) {
		if ((key == KeyCode.Z || key == KeyCode.C) && state == KeyCode.SPACE) {
			state = key;
			unsavedChanges = true;
		} else if (key.isArrowKey() && direction == KeyCode.SPACE) {
			direction = key;
			mult = 0;
			justMoved = true;
		}
	}
	
	//clear the state only if released key matches state
	public void keyRemoved(KeyCode key) {
		if (key == state) {
			state = KeyCode.SPACE;
		} else if (key == direction) {
			direction = KeyCode.SPACE;
		}
	}
	
	//save progress for later
	public void saveWork(File file) {
		try {
			//first row is dimensions and dummy filler x's
			Writer writer = new BufferedWriter(new FileWriter(file));
			writer.write(numCols + "," + numRows);
			for (int c = 1; c <= numCols - 2; c++) {
				writer.write(",x");
			}
			writer.write("\n");
			
			//bitmap from picross
			for (int r = 1; r <= numRows; r++) { 
				String[] row = new String[numCols];
				for (int c = 1; c <= numCols; c++) { 
					if (pointersToPanes[c][r].filled()) {
						row[c - 1] = "1";
					} else {
						row[c - 1] = "0";
					}
				}
				writer.write(String.join(",", row) + "\n");
			}
			
			writer.flush();
			writer.close();
			
			unsavedChanges = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//resume progress from file
	public boolean resumeWork(File file) {
		try {
			//open and skip row and col numbers
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter(",|\\n");
			for (int c = 1; c <= numCols; c++) {
				scanner.next();
			}
			
			//fill in cells from save data
			for (int r = 1; r <= numRows; r++) {
				for (int c = 1; c <= numCols; c++) {
					String next = scanner.next();
					if (next.trim().equals("1")) {
						pointersToPanes[c][r].setFilled();
					} else if (next.trim().equals("2")) {
						pointersToPanes[c][r].setUnknown();
					} else if (!next.trim().equals("0")) {
						scanner.close();
						return false;
					}
				}
			}
			
			//fix labels
			for (int r = 1; r <= numRows; r++) {
				updateLabelsRow(r);
			}
			for (int c = 1; c <= numCols; c++) {
				updateLabelsCol(c);
			}
			resizeLabels();
			
			scanner.close();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	//are there unsaved changes
	public boolean changed() {
		return unsavedChanges;
	}
	
	//output data for solver
	public int[][][] getLabels() {
		int[][] cols = new int[numCols][];
		for (int c = 0; c < numCols; c++) { 
			cols[c] = pointersToTexts[c].getNums();
		}
		
		int[][] rows = new int[numRows][];
		for (int r = 0; r < numRows; r++) { 
			rows[r] = pointersToTexts[numCols + r].getNums();
		}
		
		int[][][] headers = {cols, rows};
		return headers;
	}
	
	//get solved image of puzzle 
	public void export(File puzzle) {
		try {
			WritableImage puzzleImage = new WritableImage((int) getWidth(), (int) getHeight());
            snapshot(null, puzzleImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(puzzleImage, null);
            ImageIO.write(renderedImage, "png", puzzle);
            Desktop.getDesktop().open(puzzle);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	//get unsolved image of puzzle
	public void exportUnsolved(File puzzle) {
		int[][] grid = new int[numCols][numRows];
		for (int c = 1; c <= numCols; c++) { 
			for (int r = 1; r <= numRows; r++) { 
				if (pointersToPanes[c][r].filled()) {
					pointersToPanes[c][r].clear();
					grid[c - 1][r - 1] = 1;
				} else {
					grid[c - 1][r - 1] = 0;
				}
			}
		}
		
		export(puzzle);
		
		for (int c = 1; c <= numCols; c++) { 
			for (int r = 1; r <= numRows; r++) { 
				if (grid[c - 1][r - 1] == 1) {
					pointersToPanes[c][r].setFilled();
				} 
			}
		}
	}
	
	//display the header numbers for each row and column
	class PicrossText extends Text {
		private String separator;
		private int[] nums;
		
		public PicrossText(int[] nums, String separator) {
			this.separator = separator;
			updateText(nums);
		}
		
		public void updateText(int[] blocks) {
			this.nums = blocks;
			String newText = Arrays.stream(blocks)
					.mapToObj(String::valueOf)
					.collect(Collectors.joining(separator));
			if (separator == " ") {
				setText(newText + "\u2009");
			} else {
				setText(newText);
			}
		}
		
		public int[] getNums() {
			return nums;
		}
	}
	
	//squares of the grid to fill in
	class PicrossPane extends Pane {
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
}