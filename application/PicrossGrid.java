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
import javafx.application.Platform;
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
	private final PicrossPane[][] referencesToPanes;
	private final PicrossText[] referencesToTexts;
	private PicrossPane current;
	private KeyCode state = KeyCode.SPACE;
	private KeyCode direction = KeyCode.SPACE;
	private boolean justMoved = false;
	private boolean unsavedChanges = false;
	private final int numCols;
	private final int numRows;
	private int mult;
	public static final int cellSize = 18;
	public PicrossGrid(int numCols, int numRows, boolean editing) {
		this.numCols = numCols;
		this.numRows = numRows;
		//some pointers will be null, for consistency in indexes
		referencesToPanes = new PicrossPane[numCols + 1][numRows + 1];
		referencesToTexts = new PicrossText[numCols + numRows];
		
		//make sure top left empty space has correct borders
		PicrossPane topLeft = new PicrossPane(0, 0);
		topLeft.getStyleClass().add("nums");
		add(topLeft, 0, 0);
		referencesToPanes[0][0] = topLeft;
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
		for (int i = 1; i < numCols + 1; i++) {
			ColumnConstraints columnGrid = new ColumnConstraints(i % 5 == 1 ? cellSize + 1 : cellSize);
			getColumnConstraints().add(columnGrid);
			PicrossText nums = new PicrossText("\n");
			nums.setTextAlignment(TextAlignment.CENTER);
			referencesToTexts[i - 1] = nums;
			VBox labelCell = new VBox(nums);
			setVgrow(labelCell, Priority.ALWAYS);
			labelCell.setAlignment(Pos.BOTTOM_CENTER);
			
			//thick border every 5
			labelCell.getStyleClass().add("nums");
			if (i % 5 == 1) {labelCell.getStyleClass().add("after-fifth-column");}
            
			add(labelCell, i, 0);
		}
		
		//add row lengths and number labels
		for (int i = 1; i < numRows + 1; i++) {
			RowConstraints rowGrid = new RowConstraints(i % 5 == 1 ? cellSize + 1 : cellSize);
			getRowConstraints().add(rowGrid);
			PicrossText nums = new PicrossText(" ");
			referencesToTexts[numCols + i - 1] = nums;
			HBox labelCell = new HBox(nums);
			setHgrow(labelCell, Priority.ALWAYS);
			labelCell.setAlignment(Pos.CENTER_RIGHT);
			
			//thick border every 5
			labelCell.getStyleClass().add("nums");
			if (i % 5 == 1) {labelCell.getStyleClass().add("after-fifth-row");}
			
			add(labelCell, 0, i);
		}
		
		//add cells
		for (int c = 1; c <= numCols; c++) {
			for (int r = 1; r <= numRows; r++) {
				PicrossPane cell = new PicrossPane(c, r);
				
				//thick borders every 5
				if (c % 5 == 1) {cell.getStyleClass().add("after-fifth-column");}
				if (r % 5 == 1) {cell.getStyleClass().add("after-fifth-row");}
				
				//allow change of cursor by clicking
				if (editing) {
					cell.setOnMouseClicked(e -> {
						moveCursor(cell.getCol(), cell.getRow());
						cell.getParent().requestFocus();
					});
				}
				
				add(cell, c, r);
				referencesToPanes[c][r] = cell;
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
			current = referencesToPanes[1][1];
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
		LinkedList<Integer> colNums = new LinkedList<>();
		int tempBlock = 0;
		for (int r = 1; r <= numRows; r++) {
			if (referencesToPanes[currentCol][r].filled()) {
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
			referencesToTexts[currentCol - 1].updateText(startZero);
		} else {
			referencesToTexts[currentCol - 1].updateText(newColNums);
		}
	}
	
	//change label of a row
	private void updateLabelsRow(int currentRow) {
		LinkedList<Integer> rowNums = new LinkedList<>();
		int tempBlock = 0;
		for (int c = 1; c <= numCols; c++) {
			if (referencesToPanes[c][currentRow].filled()) {
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
			referencesToTexts[numCols + currentRow - 1].updateText(startZero);
		} else {
			referencesToTexts[numCols + currentRow - 1].updateText(newRowNums);
		}
	}
	
	//overwrite labels with puzzle labels when dislaying solver results
	public void overrideLabels(int[][][] labels) {
		int[][] colLabels = labels[0];
		int[][] rowLabels = labels[1];
		
		for (int c = 0; c < numCols; c++) {
			referencesToTexts[c].updateText(colLabels[c]);
		}
		for (int r = 0; r < numRows; r++) {
			referencesToTexts[numCols + r].updateText(rowLabels[r]);
		}
		
		resizeLabels();
	}
	
	//resize boxes holding PicrossTexts
	private void resizeLabels() {
		int newHeight = 0;
		for (int c = 0; c < numCols; c++) {
			int height = (int) referencesToTexts[c].getLayoutBounds().getHeight();
			if (height > newHeight) {
				newHeight = height;
			}
		}
		referencesToPanes[0][0].setMinHeight(newHeight + 2);
		referencesToPanes[0][0].setMaxHeight(newHeight + 2);
		for (int c = 0; c < numCols; c++) {
			((VBox) referencesToTexts[c].getParent()).setMinHeight(newHeight + 2);
			((VBox) referencesToTexts[c].getParent()).setMaxHeight(newHeight + 2);
		}
		
		int newWidth = 0;
		for (int r = 0; r < numRows; r++) {
			int width = (int) referencesToTexts[numCols + r].getLayoutBounds().getWidth();
			if (width > newWidth) {
				newWidth = width;
			}
		}
		referencesToPanes[0][0].setMinWidth(newWidth + 4);
		referencesToPanes[0][0].setMaxWidth(newWidth + 4);
		for (int r = 0; r < numRows; r++) {
			((HBox) referencesToTexts[numCols + r].getParent()).setMinWidth(newWidth + 4);
			((HBox) referencesToTexts[numCols + r].getParent()).setMaxWidth(newWidth + 4);
		}
	}
	
	//trigger move based on state and position
	private void move() {
		int currentCol = current.getCol();
		int currentRow = current.getRow();
		if (direction != null) switch (direction) {
            case RIGHT, KP_RIGHT -> moveCursor(currentCol + 1, currentRow);
            case DOWN, KP_DOWN ->   moveCursor(currentCol, currentRow + 1);
            case LEFT, KP_LEFT ->   moveCursor(currentCol - 1, currentRow);
            case UP, KP_UP ->       moveCursor(currentCol, currentRow - 1);
            default -> {}
        }
	}
	
	//change current cell
	private void moveCursor(int col, int row) {
		current.getStyleClass().remove("cursor");
		current = referencesToPanes[Math.max(1, Math.min(col, numCols))][Math.max(1, Math.min(row, numRows))];
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
		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			//first row is dimensions and dummy filler x's
			writer.write(numCols + "," + numRows);
			for (int c = 1; c <= numCols - 2; c++) {
				writer.write(",x");
			}
			writer.write("\n");
			
			//bitmap from picross
			for (int r = 1; r <= numRows; r++) { 
				String[] row = new String[numCols];
				for (int c = 1; c <= numCols; c++) { 
					if (referencesToPanes[c][r].filled()) {
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
		try (Scanner scanner = new Scanner(file)) {
			//open and skip row and col numbers
			scanner.useDelimiter(",|\\n");
			for (int c = 1; c <= numCols; c++) {
				scanner.next();
			}
			
			//fill in cells from save data
			for (int r = 1; r <= numRows; r++) {
				for (int c = 1; c <= numCols; c++) {
					String next = scanner.next().trim();
					if (next.equals("1")) {
						referencesToPanes[c][r].setFilled();
					} else if (next.equals("2")) {
						referencesToPanes[c][r].setUnknown();
					} else if (!next.equals("0")) {
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
			cols[c] = referencesToTexts[c].getNums();
		}
		
		int[][] rows = new int[numRows][];
		for (int r = 0; r < numRows; r++) { 
			rows[r] = referencesToTexts[numCols + r].getNums();
		}
		
		int[][][] headers = {cols, rows};
		return headers;
	}

	//get int matrix of puzzle
	public int[][] getMatrix(boolean clearOut) {
		int[][] matrix = new int[numCols][numRows];
		for (int c = 1; c <= numCols; c++) { 
			for (int r = 1; r <= numRows; r++) { 
				if (referencesToPanes[c][r].filled()) {
					if (clearOut) {
						referencesToPanes[c][r].clear();
					}
					matrix[c - 1][r - 1] = 1;
				} else {
					matrix[c - 1][r - 1] = 0;
				}
			}
		}
		return matrix;
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
		int[][] matrix = getMatrix(true);
		
		export(puzzle);
		
		for (int c = 1; c <= numCols; c++) { 
			for (int r = 1; r <= numRows; r++) { 
				if (matrix[c - 1][r - 1] == 1) {
					referencesToPanes[c][r].setFilled();
				} 
			}
		}
	}

	//for setting up the solver before finding information
	public void setAllUnknown() {
		for (int c = 1; c <= numCols; c++) { 
			for (int r = 1; r <= numRows; r++) { 
				referencesToPanes[c][r].getStyleClass().add("unknown");
			}
		}
	}

	//for watching the solver discover information
	//Platform.runLater() allows for thead-safe updates to the UI
	public void fillCell(int col, int row) {
		Platform.runLater( () -> {
			PicrossPane pane = referencesToPanes[col + 1][row + 1];
			pane.getStyleClass().remove("unknown");
			pane.setFilled();
		});
	}
	public void clearCell(int col, int row) {
		Platform.runLater( () -> {
			PicrossPane pane = referencesToPanes[col + 1][row + 1];
			pane.getStyleClass().remove("unknown");
			pane.clear();
		});
	}
	public void focusNums(int textIndex) {
		Platform.runLater( () -> {
			referencesToTexts[textIndex].getParent().getStyleClass().add("focused");
		});
	}
	public void unfocusNums(int textIndex) {
		Platform.runLater( () -> {
			referencesToTexts[textIndex].getParent().getStyleClass().remove("focused");
		});
	}
	
	//display the header numbers for each row and column
	class PicrossText extends Text {
		private final String separator;
		private int[] nums;
		
		public PicrossText(String separator) {
			this.separator = separator;
            if (separator.equals(" ")) {
                setText("0\u2009");
            } else {
                setText("0");
            }
            nums = new int[] {0};
		}
		
		public void updateText(int[] nums) {
			this.nums = nums;
			String newText = Arrays.stream(nums)
					.mapToObj(String::valueOf)
					.collect(Collectors.joining(separator));
			if (separator.equals(" ")) {
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
		private final int col;
		private final int row;
		private boolean filled;
		
		public PicrossPane(int col, int row) {
			this.col = col;
			this.row = row;
			getStyleClass().add("cell");
		}
		
		private int getCol() {
			return col;
		}
		
		private int getRow() {
			return row;
		}
		
		//need to remove before adding so that css tags are not added multiple times
		private void setFilled() {
			getStyleClass().remove("clear");
			getStyleClass().remove("filled");
			getStyleClass().add("filled");
			filled = true;
		}
		
		private void clear() {
			getStyleClass().remove("filled");
			getStyleClass().remove("clear");
			getStyleClass().add("clear");
			filled = false;
		}
		
		private boolean filled() {
			return filled;
		}
		
		private void setUnknown() {
			getStyleClass().add("unknown");
		}
	}
}