import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
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
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;


public class Main extends Application {
	boolean runTestCases = true;
	
	@Override
	public void start(Stage stage) {
		try {
			stage.setTitle("Nonogrammatica");
			stage.setHeight(400);
			stage.setMinHeight(400);
			stage.setWidth(400);
			stage.setMinWidth(400);
			
			BorderPane root = new BorderPane();
			
			//Set up play-mode buttons and "logo" in center of home screen
			InputStream stream = new FileInputStream("N.png");
			Image image = new Image(stream);
			ImageView logo = new ImageView(image);
			Button btnCreate = new Button("Draw a Nonogram");
			Button btnSolve = new Button("Use Nonogram Solver");
			btnCreate.setOnAction(e -> {
				getDimensions(stage, true);
			});
			btnSolve.setOnAction(e -> {
				getDimensions(stage, false);
			});
			
			VBox playmodes = new VBox(logo, btnCreate, btnSolve);
			playmodes.setSpacing(10);
			playmodes.setAlignment(Pos.CENTER);
			
			root.setCenter(playmodes);
			
			Label signature = new Label("Made by Nicholas Moorman\nnicholas.v.moorman@gmail.com");
			signature.setTextAlignment(TextAlignment.CENTER);
			HBox HBSig = new HBox(signature);
			HBSig.setAlignment(Pos.CENTER);
			root.setBottom(HBSig);
			
			//Display
			Scene homescreen = new Scene(root);
			stage.setScene(homescreen);
			stage.show();
			
			//Deselect all buttons
			btnCreate.requestFocus();
			
			if (runTestCases) {
				PicrossSolver.runTestSuite();
				runTestCases = false;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//take inputs to nonogram editor
	public void getDimensions(Stage stage, boolean drawing) {
		try {
			stage.setHeight(400);
			stage.setMinHeight(400);
			stage.setWidth(400);
			stage.setMinWidth(400);
			BorderPane root = new BorderPane();
			
			Button btnBack = new Button("Back");
			btnBack.setOnAction(e -> {
				start(stage);
			});
			btnBack.setAlignment(Pos.CENTER_LEFT);
			
			HBox buttons;
			if (drawing) {
				Pane spacer = new Pane();
				HBox.setHgrow(spacer, Priority.ALWAYS);
				
				Button btnOpen = new Button("Open From File");
				btnOpen.setOnAction(e -> {
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Select File");
					fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
					File savedWork = fileChooser.showOpenDialog(stage);
					creator(e, savedWork);
				});
				
				buttons = new HBox(btnBack, spacer, btnOpen);
			} else {
				buttons = new HBox(btnBack);
			}
			buttons.setPadding(new Insets(10,10,10,10));
			
			//tell user what to do
			String direct = "Input width and height.";
			if (drawing) {
				direct = direct + "\nMultiples of 5 are recommended for readability.";
			}
			Label instr = new Label(direct);
			instr.setTextAlignment(TextAlignment.CENTER);
			instr.setPadding(new Insets(10,10,10,10));
			
			VBox header = new VBox(buttons, instr);
			header.setAlignment(Pos.CENTER);
			root.setTop(header);
			
			//set up input fields
			Pane wSpacer1 = new Pane();
			wSpacer1.setPrefWidth(140);
			Label lblWidth = new Label("Width: ");
			TextField widthField = new TextField();
			widthField.setPrefWidth(50);
			Pane wSpacer2 = new Pane();
			wSpacer2.setPrefWidth(130);
			HBox widthBox = new HBox(lblWidth, widthField, wSpacer2);
			widthBox.setAlignment(Pos.CENTER_RIGHT);
			Label lblWidthError = new Label("");
			lblWidthError.setTextAlignment(TextAlignment.LEFT);
			lblWidthError.setPadding(new Insets(3,3,3,3));
			lblWidthError.setTextFill(Color.web("#ff0000"));
			
			Label lblHeight = new Label("Height: ");
			TextField heightField = new TextField();
			heightField.setPrefWidth(50);
			Pane hSpacer = new Pane();
			hSpacer.setPrefWidth(130);
			HBox heightBox = new HBox(lblHeight, heightField, hSpacer);
			heightBox.setAlignment(Pos.CENTER_RIGHT);
			Label lblHeightError = new Label("");
			lblHeightError.setAlignment(Pos.CENTER_LEFT);
			lblHeightError.setPadding(new Insets(3,3,8,3));
			lblHeightError.setTextFill(Color.web("#ff0000"));
			
			GridPane fields = new GridPane();
			GridPane.setConstraints(wSpacer1, 0, 0);
			GridPane.setConstraints(widthBox, 1, 0);
			GridPane.setConstraints(lblWidthError, 1, 1);
			GridPane.setConstraints(heightBox, 1, 2);
			GridPane.setConstraints(lblHeightError, 1, 3);
			fields.getChildren().addAll(wSpacer1, widthBox, lblWidthError, heightBox, lblHeightError);
			fields.setAlignment(Pos.CENTER);
			
			//submit button with function doing input data validation
			//enter button simuates button press
			Button btnSubmit = new Button("Submit");
			root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
		        if (ev.getCode() == KeyCode.ENTER) {
		        	btnSubmit.fire();
		        	ev.consume();  
		        }
		    });
			btnSubmit.setOnAction(e -> {
				String width = widthField.getText().trim();
				String height = heightField.getText().trim();
				boolean wAccept = true;
				boolean hAccept = true;
				int wInt = 0;
				int hInt = 0;
				try {
					wInt = Integer.parseInt(width);
					if (wInt <= 0) {
						lblWidthError.setText("Please input a positive integer.");
						wAccept = false;
					} else if (wInt > 100) {
						lblWidthError.setText("Maximum is 100, sorry!");
						wAccept = false;
					}
				} catch(Exception ex) {
					lblWidthError.setText("Please input a positive integer.");
					wAccept = false;
				}
				try {
					hInt = Integer.parseInt(height);
					if (hInt <= 0) {
						lblHeightError.setText("Please input a positive integer.");
						hAccept = false;
					} else if (hInt > 100) {
						lblHeightError.setText("Maximum is 100, sorry!");
						hAccept = false;
					}
				} catch(Exception ex) {
					lblHeightError.setText("Please input a positive integer.");
					hAccept = false;
				}
				if (wAccept) {lblWidthError.setText("");}
				if (hAccept) {lblHeightError.setText("");}
				if (wAccept && hAccept && drawing) {
					creator(e, wInt, hInt);
				} else if (wAccept && hAccept && !drawing) {
					getRows(stage, wInt, hInt, null, null);
				}
			});
			
			Pane bSpacer = new Pane();
			bSpacer.setPrefHeight(120);
			
			VBox inputs = new VBox(fields, btnSubmit, bSpacer);
			inputs.setAlignment(Pos.CENTER);
			root.setCenter(inputs);
			
			Scene settings = new Scene(root);
			stage.setScene(settings);
			
			widthField.requestFocus();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//open and fill existing work
	public void creator(ActionEvent event, File file) {
		try {
			Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter(",|\\n");
			
			PicrossGrid picross = creator(event, Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()));
			if (!picross.resumeWork(file)) {
				failedLoad(stage, picross);
			}
			
			scanner.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//things to do whether opening new or resuming work
	public PicrossGrid creator(ActionEvent event, int numCols, int numRows) {
		try {
			Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
			BorderPane root = new BorderPane();
			Scene creator = new Scene(root);
			PicrossGrid picross = new PicrossGrid(numCols, numRows, true);
			picross.getStylesheets().add(getClass().getResource("picross.css").toExternalForm());
			ScrollPane scrollPicross = new ScrollPane(picross);
			scrollPicross.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
			scrollPicross.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
			scrollPicross.setFitToHeight(true);
			scrollPicross.setFitToWidth(true);
			scrollPicross.setOnMouseClicked(e -> {
				picross.requestFocus();
			});
			root.setCenter(scrollPicross);
			
			Button btnBack = new Button("Back");
			btnBack.setOnAction(e -> {
				backConfirm(stage, picross);
				picross.requestFocus();
			});
			
			Pane spacer1 = new Pane();
			HBox.setHgrow(spacer1, Priority.ALWAYS);
			
			Button btnCheck = new Button("Check Solvability");
			btnCheck.setOnAction(e -> {
				displaySolvability(picross.getLabels(), picross.getMatrix(false), numCols, numRows);
				picross.requestFocus();
			});
			
			Pane spacer2 = new Pane();
			HBox.setHgrow(spacer2, Priority.ALWAYS);
			
			Button btnSave = new Button("Save");
			btnSave.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Select a Save Location");
				fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
				File savedWork = fileChooser.showSaveDialog(stage);
				picross.saveWork(savedWork);
				picross.requestFocus();
			});
			
			HBox options = new HBox(btnBack, spacer1, btnCheck, spacer2, btnSave);
			options.setPadding(new Insets(10,10,10,10));
			
			Label instr = new Label("Z: Fill in square.  C: Clear square.\n" +
					"Arrow Keys: Move cursor. Click to jump to square.");
			instr.setTextAlignment(TextAlignment.CENTER);
			instr.setPadding(new Insets(10,10,10,10));
			
			VBox header = new VBox(options, instr);
			header.setAlignment(Pos.CENTER);
			root.setTop(header);
			
			stage.setScene(creator);
			stage.sizeToScene();
			stage.setHeight(Math.max((int) Math.ceil(1.4 * numRows) * PicrossGrid.cellSize + 150, 450));
			stage.setWidth(Math.max((int) Math.ceil(1.3 * numCols) * PicrossGrid.cellSize, 400));
			
			picross.timer.start();
			
			Platform.runLater(() -> picross.requestFocus());

			return picross;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//new window with results of solver
	public void displaySolvability(int[][][] labels, int[][] solution, int numCols, int numRows) {
        try {
            Stage secondaryStage = new Stage();
			int height = Math.max((int) Math.ceil(1.4 * numRows) * PicrossGrid.cellSize + 150, 450);
            secondaryStage.setHeight(height);
            secondaryStage.setMinHeight(height);
			int width = Math.max((int) Math.ceil(1.3 * numCols) * PicrossGrid.cellSize, 400);
            secondaryStage.setWidth(width);
            secondaryStage.setMinWidth(width);

            PicrossGrid picturePicross = new PicrossGrid(numCols, numRows, false);
			picturePicross.overrideLabels(labels);
            picturePicross.getStylesheets().add(getClass().getResource("picross.css").toExternalForm());
			picturePicross.setAllUnknown();
			PicrossSolver solver = new PicrossSolver(labels, solution, picturePicross);

            BorderPane check = new BorderPane();
            Label result = new Label();
            result.setTextAlignment(TextAlignment.CENTER);
            result.setPadding(new Insets(10,10,10,10));
            check.setTop(result);

            Pane Hspacer = new Pane();
            HBox.setHgrow(Hspacer, Priority.ALWAYS);

            ScrollPane scrollPicturePicross = new ScrollPane(new Group(picturePicross));
            check.setCenter(scrollPicturePicross);

			Scene secondaryScene = new Scene(check);
            secondaryStage.setScene(secondaryScene);
            secondaryStage.show();

			new Thread() {
				public void run() {
					//The delay is only to allow for the second window to show before the solver starts going
					try {
						sleep(600);
					} catch (Exception e) {}
					boolean solvable = solver.solvable();

					//Platform.runLater is necesary for thread-safe updates to UI
					Platform.runLater(() -> {
						HBox buttons;
						if (solvable) {
							result.setText("Solvable! Do you want to export the puzzle to .png?");

							FileChooser fileChooser = new FileChooser();
							fileChooser.setTitle("Select a Save Location");
							fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG Files", "*.png"));

							Button btnEUnsolved = new Button("Export Unsolved");
							btnEUnsolved.setOnAction(ex -> {
								File unsolvedPuzzle = fileChooser.showSaveDialog(secondaryStage);
								picturePicross.exportUnsolved(unsolvedPuzzle);
							});

							Pane spacer = new Pane();
							HBox.setHgrow(spacer, Priority.ALWAYS);

							Button btnESolved = new Button("Export Solved");
							btnESolved.setOnAction(ex -> {
								File solvedPuzzle = fileChooser.showSaveDialog(secondaryStage);
								picturePicross.export(solvedPuzzle);
							});

							buttons = new HBox(btnEUnsolved, spacer, btnESolved);
						} else {
							result.setText("Not Solvable!");

							Button btnOK = new Button("OK");
							btnOK.setOnAction(ex -> {
								secondaryStage.close();
							});
							btnOK.setPadding(new Insets(10,10,10,10));
							check.setBottom(btnOK);

							buttons = new HBox(btnOK);
						}
						buttons.setPadding(new Insets(10,10,10,10));
						check.setBottom(buttons);
					});
				}
			}.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	//if save data could not be loaded
	public void failedLoad(Stage stage, PicrossGrid picross) {
		picross.timer.stop();
		
		stage.setHeight(120);
		stage.setMinHeight(120);
		stage.setWidth(200);
		stage.setMinWidth(200);
		
		Label warning = new Label("Failed to load data!");
		warning.setTextAlignment(TextAlignment.CENTER);
		warning.setPadding(new Insets(10,10,10,10));
		
		Button btnOK = new Button("OK");
		btnOK.setOnAction(e -> {
			getDimensions(stage, true);
		});
		
		VBox fail = new VBox(warning, btnOK);
		fail.setAlignment(Pos.CENTER);
		fail.setPadding(new Insets(10,10,10,10));
		
		Scene confirm = new Scene(fail);
		stage.setScene(confirm);
	}
	
	//be sure user wants to go back if saved work
	public void backConfirm(Stage stage, PicrossGrid picross) {
		if (picross.changed()) {
			Stage secondaryStage = new Stage();
			secondaryStage.setHeight(120);
			secondaryStage.setMinHeight(120);
			secondaryStage.setWidth(200);
			secondaryStage.setMinWidth(200);
			
			Label warning = new Label("Leave without saving changes?");
			warning.setTextAlignment(TextAlignment.CENTER);
			
			Button btnYes = new Button("Yes");
			btnYes.setOnAction(e -> {
				secondaryStage.close();
				picross.timer.stop();
				getDimensions(stage, true);
			});
			
			Pane spacer = new Pane();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			
			Button btnNo = new Button("No");
			btnNo.setOnAction(e -> {
				secondaryStage.close();
			});
			
			HBox buttons = new HBox(btnYes, spacer, btnNo);
			buttons.setPadding(new Insets(10,10,10,10));
			
			VBox check = new VBox(warning, buttons);
			check.setPadding(new Insets(10,10,10,10));
			
			Scene confirm = new Scene(check);
			secondaryStage.setScene(confirm);
			secondaryStage.show();
		} else {
			picross.timer.stop();
			getDimensions(stage, true);
		}
	}
	
	//get row headers from user for solver
	public void getRows(Stage stage, int numCols, int numRows, int[][] rowLabels, String[] colLabels) {
		stage.setWidth(650);
		stage.setMinWidth(650);
		stage.setHeight(650);
		stage.setMinHeight(650);
		BorderPane root = new BorderPane();
		Scene solve = new Scene(root);
		
		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			getDimensions(stage, false);
		});
		HBox HBback = new HBox(btnBack);
		HBback.setAlignment(Pos.CENTER_LEFT);
		HBback.setPadding(new Insets(10,10,10,10));
		
		//tell user what to do
		Label instr = new Label("Input row numbers separated by single spaces.\n"
				+ "You can press Tab to move to the next text box.");
		instr.setTextAlignment(TextAlignment.CENTER);
		instr.setPadding(new Insets(10,10,10,10));
		
		VBox header = new VBox(HBback, instr);
		header.setAlignment(Pos.CENTER);
		root.setTop(header);
		
		//take row numbers
		Label[] pointersToRowErrors = new Label[numRows];
		TextField[] pointersToRowFields = new TextField[numRows];
		GridPane rowInputs = getInputter(pointersToRowErrors, pointersToRowFields, true);
		ScrollPane scrollRows = new ScrollPane(rowInputs);
		root.setCenter(scrollRows);
		
		if (rowLabels != null) {
			for (int r = 0; r < numRows; r++) {
				pointersToRowFields[r].setText(Arrays.stream(rowLabels[r])
						.mapToObj(String::valueOf)
						.collect(Collectors.joining(" ")));
			}
		} 
		Button btnNext = new Button("Next");
		btnNext.setOnAction(e -> {
			int[][] newRowLabels = new int[numRows][];
			boolean acceptAll = validateInputs(pointersToRowErrors, pointersToRowFields, newRowLabels, numCols, true);
			if (acceptAll) {
				getCols(stage, numCols, numRows, newRowLabels, colLabels);
			}
		});
		HBox HBNext = new HBox(btnNext);
		HBNext.setAlignment(Pos.CENTER);
		HBNext.setPadding(new Insets(10,10,10,10));
		root.setBottom(HBNext);
		
		stage.setScene(solve);
		
		Platform.runLater(()->pointersToRowFields[0].requestFocus());
	}
	
	//get col headers from user for solver
	public void getCols(Stage stage, int numCols, int numRows, int[][] rowLabels, String[] colLabels) {
		stage.setWidth(650);
		stage.setMinWidth(650);
		stage.setHeight(650);
		stage.setMinHeight(650);
		BorderPane root = new BorderPane();
		Scene solve = new Scene(root);
		
		Button btnBack = new Button("Back");
		HBox HBBack = new HBox(btnBack);
		HBBack.setAlignment(Pos.CENTER_LEFT);
		HBBack.setPadding(new Insets(10,10,10,10));
		
		//tell user what to do
		Label instr = new Label("Input column numbers separated by single spaces.\n"
				+ "You can press Tab to move to the next text box.");
		instr.setTextAlignment(TextAlignment.CENTER);
		instr.setPadding(new Insets(10,10,10,10));
		
		VBox header = new VBox(HBBack, instr);
		header.setAlignment(Pos.CENTER);
		root.setTop(header);
		
		//take col numbers
		Label[] pointersToColErrors = new Label[numCols];
		TextField[] pointersToColFields = new TextField[numCols];
		GridPane colInputs = getInputter(pointersToColErrors, pointersToColFields, false);
		colInputs.setRotate(-90);
		ScrollPane scrollCols = new ScrollPane(new Group(colInputs));
		scrollCols.setFitToHeight(true);
		scrollCols.setFitToWidth(true);
		root.setCenter(scrollCols);
		
		btnBack.setOnAction(e -> {
			String[] newColLabels = new String[numCols];
			for (int c = 0; c < numCols; c++) {
				newColLabels[c] = pointersToColFields[c].getText();
			}
			getRows(stage, numCols, numRows, rowLabels, newColLabels);
		});

		if (colLabels != null) {
			for (int c = 0; c < numCols; c++) {
				pointersToColFields[c].setText(colLabels[c]);
			}
		}
		Button btnSubmit = new Button("Submit");
		btnSubmit.setOnAction(e -> {
			int[][] newColLabels = new int[numCols][];
			boolean acceptAll = validateInputs(pointersToColErrors, pointersToColFields, newColLabels, numRows, false);
			if (acceptAll) {
				int [][][] labels = {newColLabels, rowLabels};
				displaySolvability(labels, null, numCols, numRows);
			}
		});
		HBox HBSubmit = new HBox(btnSubmit);
		HBSubmit.setAlignment(Pos.CENTER);
		HBSubmit.setPadding(new Insets(10,10,10,10));
		root.setBottom(HBSubmit);
		
		stage.setScene(solve);
		
		Platform.runLater(()->pointersToColFields[0].requestFocus());
	}
	
	//create gridpane to take numbers for rows and columns
	public GridPane getInputter(Label[] errors, TextField[] fields, boolean rows) {
		GridPane inputs = new GridPane();
		for (int i = 0; i < errors.length; i++) {
			Label error = new Label();
			error.setPadding(new Insets(10,10,10,10));
			error.setTextFill(Color.web("#ff0000"));
			errors[i] = error;
			HBox HBError = new HBox(error);
			HBError.setAlignment(Pos.CENTER_RIGHT);
			HBError.setPrefWidth(200);
			
			TextField input = new TextField();
			input.setPrefWidth(200);
			fields[i] = input;
			
			Label lbl = new Label();
			
			Pane spacer = new Pane();
			spacer.setPrefWidth(20);
			
			if (rows) {
				lbl.setText(" Row " + (i + 1));
				input.setAlignment(Pos.CENTER_RIGHT);
			} else {
				lbl.setText(" Col " + (i + 1));
				input.setAlignment(Pos.CENTER_LEFT);
			}
			
			GridPane.setConstraints(HBError, 0, i);
			GridPane.setConstraints(input, 1, i);
			GridPane.setConstraints(lbl, 2, i);
			GridPane.setConstraints(spacer, 3, i);
			
			inputs.getChildren().addAll(HBError, input, lbl, spacer);
		}
		return inputs;
	}
	
	//make sure user input is not illogical on the face
	public boolean validateInputs(Label[] errors, TextField[] inputs, int[][] newLabels, int numPerp, boolean rows) {
		boolean acceptAll = true;
		for (int i = 0; i < errors.length; i++) {
			boolean accept = false;
			String[] temp = inputs[i].getText().trim().split(" ");
			int numsLen = temp.length;
			String[] nums = Arrays.copyOf(temp, numsLen);
			if (!rows) {
				for (int j = 0; j < numsLen / 2; j++) {
			        nums[j] = temp[numsLen - 1 - j];
			        nums[numsLen - 1 - j] = temp[j];
			    }
			}
			String num = " ";
			int[] numInts = new int[numsLen];
			int sum = 0;
			for (int n = 0; n < numsLen; n++) {
				try {
					num = nums[n];
					int numInt = Integer.parseInt(num.trim());
					numInts[n] = numInt;
					accept = true;
					if (numInt < 0) {
						errors[i].setText("Please input positive integers. ");
						inputs[i].setStyle("-fx-text-fill: red;");
						accept = false;
						break;
					} else if (numInt == 0 && numsLen > 1) {
						errors[i].setText("Please review the 0 entry here. ");
						inputs[i].setStyle("-fx-text-fill: red;");
						accept = false;
						break;
					}
					sum += numInt;
				} catch(Exception ex) {
					errors[i].setText("Please input positive integers. ");
					inputs[i].setStyle("-fx-text-fill: red;");
					accept = false;
					break;
				}
			}
			
			if (num.equals("")) {
				errors[i].setText("Please review this entry. ");
				inputs[i].setStyle("-fx-text-fill: red;");
				accept = false;
			} else if (sum + numsLen - 1 > numPerp) {
				errors[i].setText("Total of entries is too large. ");
				inputs[i].setStyle("-fx-text-fill: red;");
				accept = false;
			}
			
			if (accept) {
				errors[i].setText("");
				inputs[i].setStyle("-fx-text-fill: black;");
				newLabels[i] = numInts;
			} else {
				acceptAll = false;
			}
		}
		return acceptAll;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
