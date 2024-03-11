package Picross;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import Solver.PicrossSolver;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class PicrossEditor extends PicrossGrid {
	private PicrossPane current;
	private KeyCode state = KeyCode.SPACE;
	private KeyCode direction = KeyCode.SPACE;
	private boolean justMoved = false;
	private boolean unsavedChanges = false;

    public PicrossEditor(int rowCount, int colCount) {
        super(rowCount, colCount);

        Arrays.stream(picrossPanes)
		  	  .map(row -> Arrays.stream(row))
			  .flatMap(Function.identity())
			  .iterator()
			  .forEachRemaining(pane -> setMouseClickForCell(pane));
        
		gridPanePicross.setOnKeyPressed(keyPress -> handleKeyEntered(keyPress));
		gridPanePicross.setOnKeyReleased(keyRelease -> handleKeyReleased(keyRelease));

        current = picrossPanes[0][0];
		current.setCursor();
    }

	private void setMouseClickForCell(PicrossPane cell) {
		cell.setOnMouseClicked(e -> moveCursor(cell.getRow(), cell.getCol()));
	}

	protected void timerEvent(long lastUpdate, long time) {
		move();
		if (justMoved) {
			timerMultiplier = 6;
			justMoved = false;
		} else {
			timerMultiplier = 1;
		}
		colorCurrentSquare();
	}
	
	private void colorCurrentSquare() {
		if (state == KeyCode.C) {
			current.clear();
			fixDisplayPostColoring();
		} else if (state == KeyCode.Z) {
			current.setFilled();
			fixDisplayPostColoring();
		}
	}

	private void fixDisplayPostColoring() {
		updateRowLabels(current.getRow());
		updateColLabels(current.getCol());
		resizeLabels();
	}
	
	private void move() {
		if (direction == KeyCode.SPACE) return;
		
		int currentRow = current.getRow();
		int currentCol = current.getCol();
		
		switch (direction) {
            case RIGHT, KP_RIGHT -> moveCursor(currentRow, currentCol + 1);
            case DOWN, KP_DOWN ->   moveCursor(currentRow + 1, currentCol);
            case LEFT, KP_LEFT ->   moveCursor(currentRow, currentCol - 1);
            case UP, KP_UP ->       moveCursor(currentRow - 1, currentCol);
            default -> 				{}
        }
	}
	
	private void moveCursor(int row, int col) {
		row = Math.max(0, Math.min(row, rowCount - 1));
		col = Math.max(0, Math.min(col, colCount - 1));
		current.removeCursor();
		current = picrossPanes[row][col];
		current.setCursor();
	}
	
	public void handleKeyEntered(KeyEvent keyEvent) {
		KeyCode key = keyEvent.getCode();
		if ((key == KeyCode.Z || key == KeyCode.C) && state == KeyCode.SPACE) {
			state = key;
			unsavedChanges = true;
			colorCurrentSquare();
		} else if (key.isArrowKey() && direction == KeyCode.SPACE) {
			direction = key;
			timerMultiplier = 0;
			justMoved = true;
		}
		keyEvent.consume();
	}
	
	public void handleKeyReleased(KeyEvent keyEvent) {
		KeyCode key = keyEvent.getCode();
		if (key == state) {
			state = KeyCode.SPACE;
		} else if (key == direction) {
			direction = KeyCode.SPACE;
		}
		keyEvent.consume();
	}
	
	public void saveWork(File file) {
		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(rowCount + "," + colCount + (",x").repeat(colCount - 2) + "\n");
			
			IntStream.rangeClosed(1, rowCount)
					 .forEach(rowIndex -> saveRow(writer, rowIndex));
			
			unsavedChanges = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void saveRow(Writer writer, int rowIndex) throws RuntimeException {
		try {
			writer.write(IntStream.rangeClosed(1, colCount)
								  .map(colIndex -> picrossPanes[rowIndex][colIndex].filled() ? PicrossSolver.FILLED_CELL : PicrossSolver.CROSSED_CELL)
								  .mapToObj(String::valueOf)
								  .collect(Collectors.joining(",")) + "\n");
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public boolean resumeWork(File file) {
		try (Scanner scanner = new Scanner(file)) {
			scanner.useDelimiter("\\n");
			scanner.next();
			scanner.useDelimiter(",|\\n");

			
			Iterator<Integer> rowIterator = IntStream.range(0, rowCount).iterator();
			while (rowIterator.hasNext()) {
				int row = rowIterator.next();
				Iterator<Integer> colIterator = IntStream.range(0, colCount).iterator();
				while (colIterator.hasNext()) {
					readDataPoint(scanner.next().trim(), row, colIterator.next());
				}
			}
			
			IntStream.range(0, rowCount)
					 .iterator()
					 .forEachRemaining((IntConsumer) this::updateRowLabels);
			IntStream.range(0, colCount)
					 .iterator()
					 .forEachRemaining((IntConsumer) this::updateColLabels);
			resizeLabels();

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void readDataPoint(String data, int row, int col) throws RuntimeException {
		if (data.equals(String.valueOf(PicrossSolver.FILLED_CELL))) {
			picrossPanes[row][col].setFilled();
		} else if (!data.equals(String.valueOf(PicrossSolver.CROSSED_CELL))) {
			throw new RuntimeException("Invalid data in file: " + data);
		}
	}
	
	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}

	public int[][] getRowLabels() {
		return getLabels(rowPicrossTexts, rowCount);
	}
	
	public int[][] getColLabels() {
		return getLabels(colPicrossTexts, colCount);
	}

	private int[][] getLabels(PicrossText[] texts, int count) {
		return IntStream.range(0, count)
						.mapToObj(index -> texts[index].getNums())
						.toArray(int[][]::new);
	}
}