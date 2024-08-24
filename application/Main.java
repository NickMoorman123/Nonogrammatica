import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import Picross.PicrossEditor;
import Picross.PicrossGrid;
import Picross.PicrossViewer;
import Solver.PicrossSolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;
import javafx.geometry.Insets;


public class Main extends Application {
	private static final Color RED = Color.web("#ff0000");
	
	@Override
	public void start(Stage stage) {
		try {
			stage.getIcons().add(new Image(new FileInputStream("Icon.png")));

			stage.setTitle("Nonogrammatica");
			setStageSize(stage, 400.0, 400.0);
			
			BorderPane root = new BorderPane();
			
			ImageView logo = new ImageView(new Image(new FileInputStream("N.png")));

			Button buttonCreate = new Button("Draw a Nonogram");
			buttonCreate.setOnAction(e -> getDimensionsForEditor(stage, true));

			Button buttonSolve = new Button("Use Nonogram Solver");
			buttonSolve.setOnAction(e -> getDimensionsForEditor(stage, false));
			
			VBox vBoxPlaymodes = new VBox(logo, buttonCreate, buttonSolve);
			vBoxPlaymodes.setSpacing(10);
			vBoxPlaymodes.setAlignment(Pos.CENTER);
			
			root.setCenter(vBoxPlaymodes);
			
			Label labelSignature = new Label("Made by Nicholas Moorman\nnicholas.v.moorman@gmail.com");
			labelSignature.setTextAlignment(TextAlignment.CENTER);
			HBox hBoxSignature = new HBox(labelSignature);
			hBoxSignature.setAlignment(Pos.CENTER);
			root.setBottom(hBoxSignature);
			
			Scene sceneHomescreen = new Scene(root);
			stage.setScene(sceneHomescreen);
			stage.show();
			
			buttonCreate.requestFocus();
			
			PicrossSolver.maybeRunTests();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getDimensionsForEditor(Stage stage, boolean inDrawMode) {
		try {
			setStageSize(stage, 400.0, 400.0);

			BorderPane borderPaneRoot = new BorderPane();
			
			Button buttonBack = new Button("Back");
			buttonBack.setOnAction(e -> start(stage));
			buttonBack.setAlignment(Pos.CENTER_LEFT);
			
			HBox hBoxNavigationButtons;
			if (inDrawMode) {
				Pane paneSpacer = new Pane();
				HBox.setHgrow(paneSpacer, Priority.ALWAYS);
				
				Button buttonOpen = new Button("Open From File");
				buttonOpen.setOnAction(e -> openSavedWork(stage));
				
				hBoxNavigationButtons = new HBox(buttonBack, paneSpacer, buttonOpen);
			} else {
				hBoxNavigationButtons = new HBox(buttonBack);
			}
			hBoxNavigationButtons.setPadding(new Insets(10,10,10,10));
			
			String directions = "Input height and width." + (inDrawMode ? "\nMultiples of 5 are recommended for readability." : "\n");
			Label labelInstructions = new Label(directions);
			labelInstructions.setTextAlignment(TextAlignment.CENTER);
			labelInstructions.setPadding(new Insets(10,10,10,10));
			
			VBox vBoxHeader = new VBox(hBoxNavigationButtons, labelInstructions);
			vBoxHeader.setAlignment(Pos.CENTER);
			borderPaneRoot.setTop(vBoxHeader);
			
			Label labelHeight = new Label("Height: ");
			labelHeight.setTextAlignment(TextAlignment.RIGHT);
			labelHeight.setAlignment(Pos.CENTER_RIGHT);
			labelHeight.setMinWidth(115);

			TextField textFieldHeight = new TextField();
			textFieldHeight.setMaxWidth(50);

			Label labelHeightError = new Label("");
			labelHeightError.setTextAlignment(TextAlignment.LEFT);
			labelHeightError.setPadding(new Insets(3,0,8,0));
			labelHeightError.setTextFill(RED);
			labelHeightError.setMinWidth(165);
			
			Label labelWidth = new Label("Width: ");
			labelWidth.setTextAlignment(TextAlignment.RIGHT);
			labelWidth.setAlignment(Pos.CENTER_RIGHT);
			labelWidth.setMinWidth(115);
			
			TextField textFieldWidth = new TextField();
			textFieldWidth.setMaxWidth(50);

			Label labelWidthError = new Label("");
			labelWidthError.setTextAlignment(TextAlignment.LEFT);
			labelWidthError.setPadding(new Insets(3,0,13,0));
			labelWidthError.setTextFill(RED);
			labelWidthError.setMinWidth(165);
			
			Button buttonSubmit = new Button("Submit");
			buttonSubmit.setOnAction(e -> goToEditorOrManualInput(stage, validateDimensionInput(textFieldHeight.getText().trim(), labelHeightError),
																	validateDimensionInput(textFieldWidth.getText().trim(), labelWidthError), inDrawMode));
			
			borderPaneRoot.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
		        if (ev.getCode() == KeyCode.ENTER) {
		        	buttonSubmit.fire();
		        	ev.consume();  
		        }
		    });
			
			GridPane gridPaneInputFields = new GridPane();
			GridPane.setConstraints(labelHeight, 0, 0);
			GridPane.setConstraints(textFieldHeight, 1, 0);
			GridPane.setConstraints(labelHeightError, 1, 1);
			GridPane.setConstraints(labelWidth, 0, 2);
			GridPane.setConstraints(textFieldWidth, 1, 2);
			GridPane.setConstraints(labelWidthError, 1, 3);
			GridPane.setConstraints(buttonSubmit, 1, 4);
			gridPaneInputFields.getChildren().addAll(labelHeight, textFieldHeight, labelHeightError, labelWidth, textFieldWidth, labelWidthError);
			gridPaneInputFields.setAlignment(Pos.CENTER);

			VBox vBoxInputFields = new VBox(gridPaneInputFields, buttonSubmit);
			vBoxInputFields.setPadding(new Insets(0,0,100,0));
			vBoxInputFields.setAlignment(Pos.CENTER);

			borderPaneRoot.setCenter(vBoxInputFields);
			
			Scene sceneSettings = new Scene(borderPaneRoot);
			stage.setScene(sceneSettings);
			
			textFieldHeight.requestFocus();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private int validateDimensionInput(String input, Label error) {
		try {
			int dimension = Integer.parseInt(input);
			if (dimension <= 0) {
				throw new Exception();
			} else if (dimension > 100) {
				error.setText("Maximum is 100, sorry!");
				return 0;
			} else {
				error.setText("");
				return dimension;
			}
		} catch(Exception ex) {
			error.setText("Please input a positive integer.");
			return 0;
		}
	}

	public void goToEditorOrManualInput(Stage stage, int rowCount, int colCount, boolean inDrawMode) {
		if (rowCount > 0 && colCount > 0) {
			if (inDrawMode) {
				openEditor(stage, rowCount, colCount);
			} else {
				getManualInput(stage, rowCount, colCount);
			}
		}
	}
	
	public void openSavedWork(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select File");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
		File savedWork = fileChooser.showOpenDialog(stage);

		try (Scanner scanner = new Scanner(savedWork)) {
			scanner.useDelimiter(",|\\n");
			
			Optional<PicrossEditor> picrossEditor = openEditor(stage, Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()));
			if (picrossEditor.isEmpty()) {
				return;
			}

			if (!picrossEditor.get().resumeWork(savedWork)) {
				displayFailedLoadWindow(stage, "Failed to load data!");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void displayFailedLoadWindow(Stage stage, String failureText) {
		setStageSize(stage, 200.0, 200.0);
		
		Label labelWarning = new Label(failureText);
		labelWarning.setTextAlignment(TextAlignment.CENTER);
		labelWarning.setPadding(new Insets(10,10,10,10));
		
		Button buttonOK = new Button("OK");
		buttonOK.setOnAction(e -> getDimensionsForEditor(stage, true));
		
		VBox vBoxFailure = new VBox(labelWarning, buttonOK);
		vBoxFailure.setAlignment(Pos.CENTER);
		vBoxFailure.setPadding(new Insets(10,10,10,10));
		
		Scene confirm = new Scene(vBoxFailure);
		stage.setScene(confirm);
	}
	
	public Optional<PicrossEditor> openEditor(Stage stage, int rowCount, int colCount) {
		try {
			setStageSize(stage, Math.max(Math.ceil(1.4 * rowCount) * PicrossGrid.cellSize + 150, 450.0), Math.max(Math.ceil(1.3 * colCount) * PicrossGrid.cellSize, 400.0));
			BorderPane borderPaneRoot = new BorderPane();

			Scene sceneEditor = new Scene(borderPaneRoot);

			PicrossEditor picrossEditor = new PicrossEditor(rowCount, colCount);

			ScrollPane scrollPanePicross = picrossEditor.getScrollPaneAsParent();
			scrollPanePicross.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
			scrollPanePicross.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
			scrollPanePicross.setFitToHeight(true);
			scrollPanePicross.setFitToWidth(true);
			scrollPanePicross.setOnMouseClicked(e -> picrossEditor.requestFocus());
			borderPaneRoot.setCenter(scrollPanePicross);
			
			Button buttonBack = new Button("Back");
			buttonBack.setOnAction(e -> {
				if (picrossEditor.hasUnsavedChanges()) {
					confirmAbandonUnsavedWork(stage, picrossEditor);
				} else {
					picrossEditor.timer.stop();
					getDimensionsForEditor(stage, true);
				}
				picrossEditor.requestFocus();
			});
			
			Pane paneSpacer1 = new Pane();
			HBox.setHgrow(paneSpacer1, Priority.ALWAYS);
			
			Button buttonSolve = new Button("Check Solvability");
			buttonSolve.setOnAction(e -> {
				displaySolvability(picrossEditor.getRowLabels(), picrossEditor.getColLabels(), Optional.of(picrossEditor.getMatrix()), rowCount, colCount, Optional.of(stage.heightProperty().doubleValue()), Optional.of(stage.widthProperty().doubleValue()));
				picrossEditor.requestFocus();
			});
			
			Pane paneSpacer2 = new Pane();
			HBox.setHgrow(paneSpacer2, Priority.ALWAYS);
			
			Button buttonSave = new Button("Save");
			buttonSave.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Select a Save Location");
				fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));

				File savedWork = fileChooser.showSaveDialog(stage);
				picrossEditor.saveWork(savedWork);
				picrossEditor.requestFocus();
			});
			
			HBox hBoxOptions = new HBox(buttonBack, paneSpacer1, buttonSolve, paneSpacer2, buttonSave);
			hBoxOptions.setPadding(new Insets(10,10,10,10));
			
			Label labelInstructions = new Label("Z: Fill in square.  C: Clear square.\n" +
					"Arrow Keys: Move cursor. Click to jump to square.");
			labelInstructions.setTextAlignment(TextAlignment.CENTER);
			labelInstructions.setPadding(new Insets(10,10,10,10));
			
			VBox vBoxHeader = new VBox(hBoxOptions, labelInstructions);
			vBoxHeader.setAlignment(Pos.CENTER);
			borderPaneRoot.setTop(vBoxHeader);
			
			stage.setScene(sceneEditor);
			stage.setOnCloseRequest(e -> picrossEditor.timer.stop());
			
			picrossEditor.timer.start();

			picrossEditor.requestFocus();
			Platform.runLater(() -> picrossEditor.requestFocus());

			return Optional.of(picrossEditor);
		} catch(Exception e) {
			e.printStackTrace();
			displayFailedLoadWindow(stage, "Failed to open editor!\nI'm not sure how you got here ¯\\_(ツ)_/¯");
			return Optional.empty();
		}
	}
	
	public void displaySolvability(int[][] rowHeaders, int[][] colHeaders, Optional<int[][]> solution, int rowCount, int colCount, Optional<Double> height, Optional<Double> width) {
        try {
            Stage secondaryStage = new Stage();
			setStageSize(secondaryStage, height.orElse(Math.max(Math.ceil(1.4 * rowCount) * PicrossGrid.cellSize + 150.0, 450.0)), width.orElse(Math.max(Math.ceil(1.3 * colCount) * PicrossGrid.cellSize, 400.0)));

            BorderPane borderPaneCheck = new BorderPane();
            Label labelResult = new Label();
            labelResult.setTextAlignment(TextAlignment.CENTER);
            labelResult.setPadding(new Insets(10,10,10,10));
			labelResult.setText("Solving...");
            borderPaneCheck.setTop(labelResult);

            Pane paneSpacer1 = new Pane();
            HBox.setHgrow(paneSpacer1, Priority.ALWAYS);

			PicrossViewer picrossViewer = new PicrossViewer(rowCount, colCount, rowHeaders, colHeaders, solution);
            ScrollPane scrollPanePicturePicross = new ScrollPane(picrossViewer.getGroupAsParent());
            borderPaneCheck.setCenter(scrollPanePicturePicross);

			Scene secondaryScene = new Scene(borderPaneCheck);
            secondaryStage.setScene(secondaryScene);
			secondaryStage.setOnCloseRequest(e -> picrossViewer.timer.stop());
            secondaryStage.show();

			Platform.runLater(() -> picrossViewer.timer.start());

			picrossViewer.setUIUpdateOnComplete(() -> updateSolverWindowWhenFinished(secondaryStage, borderPaneCheck, labelResult, picrossViewer));
			picrossViewer.setUIUpdateAddSkipAnimationButton(() -> addSkipSolverAnimationButton(borderPaneCheck, picrossViewer));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void updateSolverWindowWhenFinished(Stage secondaryStage, BorderPane borderPaneCheck, Label labelResult, PicrossViewer picrossViewer) {
		HBox hBoxButtons;
		String guessTag = picrossViewer.triedGuessing() ? " with guess and check" : "";
		if (picrossViewer.getSolverResult().get()) {
			labelResult.setText("Solvable" + guessTag + "! Do you want to export the puzzle to .png?");

			Button buttonExportUnsolved = new Button("Export Unsolved");
			buttonExportUnsolved.setOnAction(ex -> picrossViewer.exportUnsolvedImage(secondaryStage));

			Pane paneSpacer2 = new Pane();
			HBox.setHgrow(paneSpacer2, Priority.ALWAYS);

			Button buttonExportSolved = new Button("Export Solved");
			buttonExportSolved.setOnAction(ex -> picrossViewer.exportImage(secondaryStage));

			hBoxButtons = new HBox(buttonExportUnsolved, paneSpacer2, buttonExportSolved);
		} else {
			labelResult.setText("Not solvable" + guessTag + "!");

			Button buttonGuess = new Button("Try guess and check");
			buttonGuess.setOnAction(ex -> doGuessAndCheck(borderPaneCheck, labelResult, picrossViewer));

			Pane paneSpacer2 = new Pane();
			HBox.setHgrow(paneSpacer2, Priority.ALWAYS);

			Button buttonOK = new Button("OK");
			buttonOK.setOnAction(ex -> secondaryStage.close());

			hBoxButtons = !picrossViewer.triedGuessing() ? new HBox(buttonGuess, paneSpacer2, buttonOK) : new HBox(paneSpacer2, buttonOK);

			borderPaneCheck.setBottom(hBoxButtons);
		}
		hBoxButtons.setPadding(new Insets(10,10,10,10));
		borderPaneCheck.setBottom(hBoxButtons);
	}

	public void doGuessAndCheck(BorderPane borderPaneCheck, Label labelResult, PicrossViewer picrossViewer) {
		borderPaneCheck.setBottom(new HBox());
		labelResult.setText("Solving...");
		picrossViewer.tryGuessAndCheck();
	}

	public void addSkipSolverAnimationButton(BorderPane borderPaneCheck, PicrossViewer picrossViewer) {
		Button buttonSkipAnimation = new Button("Skip Animation");

		HBox hBoxSkipButton = new HBox(buttonSkipAnimation);
		hBoxSkipButton.setPadding(new Insets(10,10,10,10));
		
		borderPaneCheck.setBottom(hBoxSkipButton);

		buttonSkipAnimation.setOnAction(ex -> {
			picrossViewer.skipRemainingAnimation();
			buttonSkipAnimation.setDisable(true);
			borderPaneCheck.setBottom(new Pane());
		});
	}
	
	public void confirmAbandonUnsavedWork(Stage stage, PicrossEditor picrossEditor) {
		Stage secondaryStage = new Stage();
		setStageSize(secondaryStage, 120.0, 200.0);
		
		Label buttonWarning = new Label("Leave without saving changes?");
		buttonWarning.setTextAlignment(TextAlignment.CENTER);
		
		Button buttonYes = new Button("Yes");
		buttonYes.setOnAction(e -> {
			secondaryStage.close();
			picrossEditor.timer.stop();
			getDimensionsForEditor(stage, true);
		});
		
		Pane paneSpacer = new Pane();
		HBox.setHgrow(paneSpacer, Priority.ALWAYS);
		
		Button buttonNo = new Button("No");
		buttonNo.setOnAction(e -> secondaryStage.close());
		
		HBox hBoxButtons = new HBox(buttonYes, paneSpacer, buttonNo);
		hBoxButtons.setPadding(new Insets(10,10,10,10));
		
		VBox vboxCheck = new VBox(buttonWarning, hBoxButtons);
		vboxCheck.setPadding(new Insets(10,10,10,10));
		
		Scene confirm = new Scene(vboxCheck);
		secondaryStage.setScene(confirm);
		secondaryStage.show();
	}

	public void getManualInput(Stage stage, int rowCount, int colCount) {
		setStageSize(stage, 650.0, 650.0);
		
		BorderPane borderPaneRoot = new BorderPane();
		Scene scene = new Scene(borderPaneRoot);
		
		Button buttonBack = new Button("Back");
		HBox hBoxBack = new HBox(buttonBack);
		hBoxBack.setAlignment(Pos.CENTER_LEFT);
		hBoxBack.setPadding(new Insets(10,10,10,10));
		
		Label labelInstructions = new Label("Input numbers.\nYou can press Tab to move to the next box.");
		labelInstructions.setTextAlignment(TextAlignment.CENTER);
		labelInstructions.setPadding(new Insets(10,10,10,10));
		
		VBox vBoxHeader = new VBox(hBoxBack, labelInstructions);
		vBoxHeader.setAlignment(Pos.CENTER);
		borderPaneRoot.setTop(vBoxHeader);

		InputAndError[] rowFields = new InputAndError[rowCount];
		GridPane gridPaneRowInputs = getRowInputter(rowFields);
		ScrollPane scrollPaneRowFields = new ScrollPane(gridPaneRowInputs);

		InputAndError[] colFields = new InputAndError[colCount];
		GridPane gridPaneColInputs = getColInputter(colFields);
		gridPaneColInputs.setRotate(-90);
		ScrollPane scrollPaneColFields = new ScrollPane(new Group(gridPaneColInputs));
		scrollPaneColFields.setFitToHeight(true);
		scrollPaneColFields.setFitToWidth(true);

		Button buttonNext = new Button("Next");
		HBox HBNext = new HBox(buttonNext);
		HBNext.setAlignment(Pos.CENTER);
		HBNext.setPadding(new Insets(10,10,10,10));
		borderPaneRoot.setBottom(HBNext);
		
		stage.setScene(scene);

		int[][] rowInputs = new int[rowCount][];
		int[][] colInputs = new int[colCount][];
		setManualInputToTakeRows(stage, borderPaneRoot, buttonBack, buttonNext, rowCount, colCount, rowFields, scrollPaneRowFields, colFields, scrollPaneColFields, rowInputs, colInputs);
	}
	
	private void setManualInputToTakeRows(Stage stage, BorderPane borderPaneRoot, Button buttonBack, Button buttonNext, int rowCount, int colCount, InputAndError[] rowFields, ScrollPane scrollPaneRowFields, InputAndError[] colFields, ScrollPane scrollPaneColFields, int[][] rowInputs, int[][] colInputs) {
		buttonBack.setOnAction(e -> getDimensionsForEditor(stage, false));

		buttonNext.setOnAction(e -> setManualInputToTakeCols(stage, borderPaneRoot, buttonBack, buttonNext, rowCount, colCount, rowFields, scrollPaneRowFields, colFields, scrollPaneColFields, rowInputs, colInputs));

		borderPaneRoot.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				buttonNext.fire();
				ev.consume();  
			}
		});
		
		borderPaneRoot.setCenter(scrollPaneRowFields);
		
		Platform.runLater(() -> rowFields[0].Input.requestFocus());
	}

	private void setManualInputToTakeCols(Stage stage, BorderPane borderPaneRoot, Button buttonBack, Button buttonNext, int rowCount, int colCount, InputAndError[] rowFields, ScrollPane scrollPaneRowFields, InputAndError[] colFields, ScrollPane scrollPaneColFields, int[][] rowInputs, int[][] colInputs) {
		if (!validateInputs(rowFields, rowInputs, colCount)) {
			return;
		}
		
		buttonBack.setOnAction(e -> setManualInputToTakeRows(stage, borderPaneRoot, buttonBack, buttonNext, rowCount, colCount, rowFields, scrollPaneRowFields, colFields, scrollPaneColFields, rowInputs, colInputs));

		buttonNext.setOnAction(e -> {
			if (validateInputs(colFields, colInputs, rowCount)) {
				displaySolvability(rowInputs, colInputs, Optional.empty(), rowCount, colCount, Optional.empty(), Optional.empty());
			}
		});

		borderPaneRoot.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				buttonNext.fire();
				ev.consume();  
			}
		});
		
		borderPaneRoot.setCenter(scrollPaneColFields);
		
		Platform.runLater(() -> colFields[0].Input.requestFocus());
	}

	public GridPane getRowInputter(InputAndError[] inputs) {
		return getInputter(inputs, " Row ");
	}

	public GridPane getColInputter(InputAndError[] inputs) {
		return getInputter(inputs, " Col ");
	}
	
	public GridPane getInputter(InputAndError[] fields, String rowOrCol) {
		GridPane gridPaneInputs = new GridPane();
		for (int i = 0; i < fields.length; i++) {
			fields[i] = new InputAndError();

			fields[i].Error.setPadding(new Insets(10,10,10,10));
			fields[i].Error.setTextFill(RED);
			HBox hBoxError = new HBox(fields[i].Error);
			hBoxError.setAlignment(Pos.CENTER_RIGHT);
			hBoxError.setPrefWidth(200);
			
			fields[i].Input.setPrefWidth(200);
			
			Pane spacer = new Pane();
			spacer.setPrefWidth(20);
			
			Label labelInput = new Label();
			labelInput.setText(rowOrCol + (i + 1));
			labelInput.setAlignment(Pos.CENTER_RIGHT);
			
			GridPane.setConstraints(hBoxError, 0, i);
			GridPane.setConstraints(fields[i].Input, 1, i);
			GridPane.setConstraints(labelInput, 2, i);
			GridPane.setConstraints(spacer, 3, i);
			
			gridPaneInputs.getChildren().addAll(hBoxError, fields[i].Input, labelInput, spacer);
		}
		return gridPaneInputs;
	}
	
	public boolean validateInputs(InputAndError[] fields, int[][] inputs, int maxLength) {
		boolean accept = true;
		for (int inputIndex = 0; inputIndex < fields.length; inputIndex++) {
			// regex splits string by any sections of non-numeric characters
			String[] nums = fields[inputIndex].Input.getText().split("[^0-9]+");
			int numCount = nums.length;
			if (numCount > 1 && nums[0].equals("")) {
				nums = Arrays.copyOfRange(nums, 1, numCount);
				numCount--;
			}

			fields[inputIndex].Input.setText(String.join(" ", nums));
			int numIndex = 0;

			if (fields[inputIndex].Input.getText().length() == 0) {
				accept = setManualInputError("Please input numbers.", fields[inputIndex]);
				continue;
			}

			int sum = 0;
			int[] numInts = new int[numCount];
			boolean hasZero = false;
			while (numIndex < numCount) {
				int numInt = Integer.parseInt(nums[numIndex]);
				sum += numInt;
				numInts[numIndex] = numInt;

				if (numInt == 0 && numCount > 1) {
					hasZero = true;
					break;
				}
				numIndex++;
			}
			if (hasZero) {
				accept = setManualInputError("Please review the 0 entry here.", fields[inputIndex]);
				continue;
			}

			if (sum + numCount - 1 > maxLength) {
				accept = setManualInputError("Total of entries is too large.", fields[inputIndex]);
				continue;
			}
			
			fields[inputIndex].Error.setText("");
			fields[inputIndex].Input.setStyle("-fx-text-fill: black;");
			inputs[inputIndex] = numInts;
		}
		return accept;
	}

	private boolean setManualInputError(String error, InputAndError field) {
		field.Error.setText(error.trim() + " ");
		field.Input.setStyle("-fx-text-fill: red;");
		return false;
	}

	private void setStageSize(Stage stage, double height, double width) {
		stage.setHeight(height);
		stage.setWidth(width);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
