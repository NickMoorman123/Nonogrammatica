package Picross;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.PrimitiveIterator;
import java.util.function.Function;
import java.util.stream.IntStream;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public abstract class PicrossGrid {
	protected GridPane gridPanePicross = new GridPane();
	protected PicrossPane[][] picrossPanes;
	protected RowText[] rowPicrossTexts;
	protected ColumnText[] colPicrossTexts;
	private PicrossPane topLeft;
	protected final int rowCount;
	protected final int colCount;
	public static final int cellSize = 18;
	public AnimationTimer timer;
	protected int timerMultiplier = 1;

	public PicrossGrid(int rowCount, int colCount) {
		this.rowCount = rowCount;
		this.colCount = colCount;
		picrossPanes = new PicrossPane[rowCount][colCount];
		rowPicrossTexts = new RowText[rowCount];
		colPicrossTexts = new ColumnText[colCount];
		gridPanePicross.setOnMouseClicked(e -> gridPanePicross.requestFocus());
	
		timer = new AnimationTimer() {
			private long lastUpdate = 0;

			@Override
			public void start() {
				Platform.runLater(() -> gridPanePicross.requestFocus());
				super.start();
			}
			
			@Override
			public void handle(long time) {
				if (time - lastUpdate < timerMultiplier * 40_000_000) {
					return;
				}
				
				timerEvent(lastUpdate, time);
				lastUpdate = time;
			}
		};
		
		addAndStyleCells();
		
		resizeLabels();
		getStylesheets().add(getClass().getResource("picross.css").toExternalForm());
	}

	protected abstract void timerEvent(long lastUpdate, long time);

	private void addAndStyleCells() {
		topLeft = new PicrossPane(-1, -1);
		topLeft.getStyleClass().add("nums");
		HBox topLeftBox = new HBox(topLeft);
		topLeftBox.setAlignment(Pos.BOTTOM_RIGHT);
		gridPanePicross.add(topLeftBox, 0, 0);
		
		ColumnConstraints rowLabels = new ColumnConstraints();
		rowLabels.setHalignment(HPos.RIGHT);
		getColumnConstraints().add(rowLabels);

		RowConstraints columnLabels = new RowConstraints();
		columnLabels.setValignment(VPos.BOTTOM);
		getRowConstraints().add(columnLabels);
		
		PrimitiveIterator.OfInt rowIterator = IntStream.range(0, rowCount).iterator();
		while (rowIterator.hasNext()) {
			int rowIndex = rowIterator.next();
			addRowProperties(rowIndex);
			addARowOfCells(rowIndex);
		}
		
		PrimitiveIterator.OfInt colIterator = IntStream.range(0, colCount).iterator();
		while (colIterator.hasNext()) {
			addColumnProperties(colIterator.next());
		}
	}

	private void addRowProperties(int rowIndex) {
		RowConstraints rowGrid = new RowConstraints(rowIndex % 5 == 0 ? cellSize + 1 : cellSize);
		getRowConstraints().add(rowGrid);

		RowText nums = new RowText();
		rowPicrossTexts[rowIndex] = nums;
		HBox labelCell = new HBox(nums);
		setHgrow(labelCell, Priority.ALWAYS);
		labelCell.setAlignment(Pos.CENTER_RIGHT);
		
		labelCell.getStyleClass().add("nums");
		if (rowIndex % 5 == 0) {labelCell.getStyleClass().add("after-fifth-row");}
		
		gridPanePicross.add(labelCell, 0, rowIndex + 1);
	}

	private void addColumnProperties(int colIndex) {
		ColumnConstraints columnGrid = new ColumnConstraints(colIndex % 5 == 0 ? cellSize + 1 : cellSize);
		getColumnConstraints().add(columnGrid);

		ColumnText nums = new ColumnText();
		nums.setTextAlignment(TextAlignment.CENTER);
		colPicrossTexts[colIndex] = nums;
		VBox labelCell = new VBox(nums);
		setVgrow(labelCell, Priority.ALWAYS);
		labelCell.setAlignment(Pos.BOTTOM_CENTER);
		
		labelCell.getStyleClass().add("nums");
		if (colIndex % 5 == 0) {labelCell.getStyleClass().add("after-fifth-column");}
		
		gridPanePicross.add(labelCell, colIndex + 1, 0);
	}

	private void addARowOfCells(int row) {
		PrimitiveIterator.OfInt colIterator = IntStream.range(0, colCount).iterator();
		while (colIterator.hasNext()) {
			addCell(row, colIterator.next());
		}
	}

	private PicrossPane addCell(int rowIndex, int colIndex) {
		PicrossPane picrossPane = new PicrossPane(rowIndex, colIndex);
		picrossPanes[rowIndex][colIndex] = picrossPane;
		gridPanePicross.add(picrossPane, colIndex + 1, rowIndex + 1);
		return picrossPane;
	}
	
	protected void updateRowLabels(int rowIndex) {
		rowPicrossTexts[rowIndex].updateText(collectNums(colCount, colIndex -> picrossPanes[rowIndex][colIndex]));
	}
	
	protected void updateColLabels(int colIndex) {
		colPicrossTexts[colIndex].updateText(collectNums(rowCount, rowIndex -> picrossPanes[rowIndex][colIndex]));
	}

	private int[] collectNums(int max, Function<Integer, PicrossPane> getPane) {
		LinkedList<Integer> nums = new LinkedList<>();
		int tempBlock = 0;
		int index = 0;
		while (index < max) {
			if (getPane.apply(index).filled()) {
				tempBlock++;
			} else if (tempBlock > 0) {
				nums.add(tempBlock);
				tempBlock = 0;
			}
			index++;
		}
		if (tempBlock > 0) {
			nums.add(tempBlock);
			tempBlock = 0;
		}

		if (nums.size() == 0) {
			nums.add(0);
		}

		return nums.stream()
				   .mapToInt(Integer::intValue)
				   .toArray();
	}
	
	protected void resizeLabels() {
		double newHeight = Arrays.stream(colPicrossTexts)
								 .mapToDouble(text -> text.getLayoutBounds().getHeight())
								 .max()
								 .orElse(cellSize)
								 + 2.0;

		topLeft.setMinHeight(newHeight);
		topLeft.setMaxHeight(newHeight);
		for (ColumnText text : colPicrossTexts) {
			text.setHeight(newHeight);
		}
			
		double newWidth = Arrays.stream(rowPicrossTexts)
								 .mapToDouble(text -> text.getLayoutBounds().getWidth())
								 .max()
								 .orElse(cellSize)
								 + 4.0;

		topLeft.setMinWidth(newWidth);
		topLeft.setMaxWidth(newWidth);
		for (RowText text : rowPicrossTexts) {
			text.setWidth(newWidth);
		}
	}

	public ObservableList<RowConstraints> getRowConstraints() {
		return gridPanePicross.getRowConstraints();
	}
	
	public ObservableList<ColumnConstraints> getColumnConstraints() {
		return gridPanePicross.getColumnConstraints();
	}

	public WritableImage snapshot(SnapshotParameters arg0, WritableImage arg1) {
		return gridPanePicross.snapshot(arg0, arg1);
	}

	public double getWidth() {
		return gridPanePicross.getWidth();
	}

	public double getHeight() {
		return gridPanePicross.getHeight();
	}

	public void setVgrow(Node arg0, Priority arg1) {
		GridPane.setVgrow(arg0, arg1);
	}

	public void setHgrow(Node arg0, Priority arg1) {
		GridPane.setHgrow(arg0, arg1);
	}

	public ObservableList<String> getStylesheets() {
		return gridPanePicross.getStylesheets();
	}

	public void requestFocus() {
		gridPanePicross.requestFocus();
	}

	public ScrollPane getScrollPaneAsParent() {
		return new ScrollPane(gridPanePicross);
	}

	public Group getGroupAsParent() {
		return new Group(gridPanePicross);
	}
}