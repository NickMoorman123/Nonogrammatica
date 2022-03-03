package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;

public class PicrossSolver {
	private int[][] colHeaders;
	private int[][] rowHeaders;
	private int numCols;
	private int numRows;
	private int[][] grid;
	private boolean impossible = false;
	public PicrossSolver(int[][][] headers) {
		colHeaders = headers[0];
		rowHeaders = headers[1];
		numCols = colHeaders.length;
		numRows = rowHeaders.length;
		grid = new int[numCols][numRows];
		for (int c = 0; c < numCols; c++) {
			for (int r = 0; r < numRows; r++) {
				grid[c][r] = 2;
			}
		}
		
		iterate();
	}
	
	//loop over all rows and columns until the loop when
	//no changes have been made
	private void iterate() {
		try {
			boolean changed;
			do {
				changed = false;
				for (int c = 0; c < numCols; c++) {
					if (colUpdate(c)) {
						changed = true;
					}
				}
				for (int r = 0; r < numRows; r++) {
					if (rowUpdate(r)) {
						changed = true;
					}
				}
			} while (changed);
		} catch(Exception e) {
			impossible = true;
		}
	}
	
	//look for changes to make to a column
	@SuppressWarnings("null")
	private boolean colUpdate(int col) {
		try {
			boolean changed = false;
			
			//get current state
			int[] line = new int[numRows];
			for (int r = 0; r < numRows; r++) {
				line[r] = grid[col][r];
			}
			//determine any new info
			PicrossSolverLine solver = new PicrossSolverLine(line, colHeaders[col]);
			int[] newline = solver.attempt();
			
			//post new info and call the perpendiculars
			boolean[] rowsToUpdate = new boolean[numRows];
			Arrays.fill(rowsToUpdate, false);
			for (int r = 0; r < numRows; r++) {
				if (newline[r] != line[r]) {
					if (line[r] == 2) {
						grid[col][r] = newline[r];
						changed = true;
						rowsToUpdate[r] = true;
					} else {
						//should not be rewriting or erasing
						return (Boolean) null;
					}
				}
			}
			for (int r = 0; r < numRows; r++) {
				if (rowsToUpdate[r]) {
					rowUpdate(r);
				}
			}
			
			return changed;
		} catch(Exception e) {
			return (Boolean) null;
		}
	}
	
	//look for changes to make to a row
	@SuppressWarnings("null")
	private boolean rowUpdate(int row) {
		try {
			boolean changed = false;
			
			//get current state
			int[] line = new int[numCols];
			for (int c = 0; c < numCols; c++) {
				line[c] = grid[c][row];
			}
			
			//determine any new info
			PicrossSolverLine solver = new PicrossSolverLine(line, rowHeaders[row]);
			int[] newline = solver.attempt();
			
			//post new info and call the perpendiculars
			boolean[] colsToUpdate = new boolean[numCols];
			Arrays.fill(colsToUpdate, false);
			for (int c = 0; c < numCols; c++) {
				if (newline[c] != line[c]) {
					if (line[c] == 2) {
						grid[c][row] = newline[c];
						changed = true;
						colsToUpdate[c] = true;
					} else {
						return (Boolean) null;
					}	
				} 
			}
			for (int c = 0; c < numCols; c++) {
				if (colsToUpdate[c]) {
					colUpdate(c);
				}
			}
			
			return changed;
		} catch(Exception e) {
			return (Boolean) null;
		}
	}
	
	//get whether it is solvable
	public boolean solvable() {
		if (impossible) {
			return false;
		}
		for (int c = 0; c < numCols; c++) {
			for (int r = 0; r < numRows; r++) {
				if (grid[c][r] == 2) {
					return false;
				}
			}
		}
		return true;
	}
	
	//give end result
	public void getResult(File file) {
		try {
			//first row is dimensions and dummy filler x's
			Writer writer = new BufferedWriter(new FileWriter(file));
			writer.write(numCols + "," + numRows);
			for (int c = 1; c <= numCols - 2; c++) {
				writer.write(",x");
			}
			writer.write("\n");
			
			//bitmap from picross
			for (int r = 0; r < numRows; r++) { 
				String[] row = new String[numCols];
				for (int c = 0; c < numCols; c++) { 
					row[c] = "" + grid[c][r];
				}
				writer.write(String.join(",", row) + "\n");
			}
			
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}