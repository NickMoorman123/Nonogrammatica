package Picross;

import java.awt.Desktop;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import Solver.PicrossSolver;
import Solver.SolutionDisplayStep;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class PicrossViewer extends PicrossGrid {
	private PicrossSolver picrossSolver;
	private ExecutorService solverExecutor;
    private ConcurrentLinkedQueue<SolutionDisplayStep> solutionDisplaySteps;
	private PicrossText currentPicrossText;
	private Runnable runnableOnComplete;
	private Runnable runnableAddSkipAnimationButton;
	private boolean addedButton = false;
	private boolean triedGuessing = false;
	private Optional<Boolean> solverResult = Optional.empty();

    public PicrossViewer(int rowCount, int colCount, int[][] rowHeaders, int[][] colHeaders, Optional<int[][]> solution) {
        super(rowCount, colCount);
		picrossSolver = new PicrossSolver(rowHeaders, colHeaders, solution);

		IntStream.range(0, rowCount)
				 .iterator()
				 .forEachRemaining((IntConsumer) row -> rowPicrossTexts[row].updateText(rowHeaders[row]));
		IntStream.range(0, colCount)
				 .iterator()
				 .forEachRemaining((IntConsumer) col -> colPicrossTexts[col].updateText(colHeaders[col]));
		
		resizeLabels();

		currentPicrossText = rowPicrossTexts[0];

		solverExecutor = Executors.newSingleThreadExecutor();
		solverExecutor.submit(() -> solverResult = Optional.of(picrossSolver.solvable()));

		solutionDisplaySteps = picrossSolver.getSolutionSteps();
    }

	public void setUIUpdateOnComplete(Runnable onComplete) {
		runnableOnComplete = onComplete;
	}

	public void setUIUpdateAddSkipAnimationButton(Runnable skipAnimation) {
		runnableAddSkipAnimationButton = skipAnimation;
	}

	protected void timerEvent(long lastUpdate, long time) {
		SolutionDisplayStep displayStep = solutionDisplaySteps.poll();

		if (solverResult.isPresent()) { 
			if (!addedButton) {
				Platform.runLater(runnableAddSkipAnimationButton);
				addedButton = true;
			} else if (displayStep == null) {
				currentPicrossText.removeFocused();
				timer.stop();
				addedButton = false;
				Platform.runLater(runnableOnComplete);
				return;
			}
		}

		if (displayStep == null) {
			return;
		}
		
		currentPicrossText.removeFocused();
		currentPicrossText = (PicrossText) (displayStep.focusColNums ? colPicrossTexts[displayStep.col] : rowPicrossTexts[displayStep.row]);
		currentPicrossText.setFocused();

		updatePane(displayStep);
	}

	public void skipRemainingAnimation() {
		timer.stop();
		currentPicrossText.removeFocused();

		while (!solutionDisplaySteps.isEmpty()) {
			updatePane(solutionDisplaySteps.poll());
		}
		
		Platform.runLater(runnableOnComplete);
	}

	private void updatePane(SolutionDisplayStep displayStep) {
		if (displayStep.newState == PicrossSolver.FILLED_CELL) {
			picrossPanes[displayStep.row][displayStep.col].setFilled();
		} else if (displayStep.newState == PicrossSolver.CROSSED_CELL) {
			picrossPanes[displayStep.row][displayStep.col].setCrossed();
		} else {
			throw new RuntimeException("Invalid new state for " + displayStep.row + " " + displayStep.col);
		}
	}

	public Optional<Boolean> getSolverResult() {
		return solverResult;
	}

	public void tryGuessAndCheck() {
		triedGuessing = true;
		solverResult = Optional.empty();
		addedButton = false;
		solverExecutor.submit(() -> solverResult = Optional.of(picrossSolver.solvableWithGuessAndCheck()));
		timer.start();
	}

	public boolean triedGuessing() {
		return triedGuessing;
	}
	
	public void exportImage(Stage stage) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select a Save Location");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG Files", "*.png"));

			File puzzle = fileChooser.showSaveDialog(stage);

			WritableImage puzzleImage = new WritableImage((int) getWidth(), (int) getHeight());
            snapshot(null, puzzleImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(puzzleImage, null);
            ImageIO.write(renderedImage, "png", puzzle);
            Desktop.getDesktop().open(puzzle);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
	}
	
	public void exportUnsolvedImage(Stage stage) {
		LinkedList<PicrossPane> panesToRefill = new LinkedList<>();
		LinkedList<PicrossPane> panesToRecross = new LinkedList<>();
		for (PicrossPane[] paneRow : picrossPanes) {
			for (PicrossPane pane : paneRow) {
				if (pane.filled()) {
					pane.clear();
					panesToRefill.add(pane);
				} else {
					pane.uncross();
					panesToRecross.add(pane);
				}
			}
		}
			
		exportImage(stage);
		
		for (PicrossPane pane : panesToRefill) {
			pane.setFilled();
		}
		for (PicrossPane pane : panesToRecross) {
			pane.recross();
		}
	}
}
