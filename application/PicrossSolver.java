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
	
	//used to glean any new info on a line
	class PicrossSolverLine {
		private int[] line;
		private int[] nums;
		private int len;
		private int numsLen;
		public PicrossSolverLine(int[] line, int[] nums) {
			this.line = line;
			this.nums = nums;
			len = line.length;
			numsLen = nums.length;
		}
		
		//find possibilities based on new info
		public int[] attempt() {
			try {
				if (nums[0] == 0) {
					int[] newline = new int[len];
					Arrays.fill(newline, 0);
					return newline;
				} else {
					//furthest left/up possibility
					int[] leftExtreme = getExtreme(1);
					//furthest right/down possibility
					int[] rightExtreme = getExtreme(-1);
					
					return getNewInfo(leftExtreme, rightExtreme);
				}
			} catch (Exception e) {
				return null;
			}
		}
		
		private int[] getExtreme(int dir) {
			try {
				int index;
				int numIndex;
				int extreme[] = new int[numsLen];
				
				//set up start positions
				if (dir == 1) {
					index = 0;
					numIndex = 0;
				} else {
					index = len - 1;
					numIndex = numsLen - 1;
				}
				while (numIndex >= 0 && numIndex < numsLen) {
					int num = nums[numIndex];
					//check that there's room
					while (!theresRoom(index, num, dir)) {
						index += dir;
					}
					//take down left/right index of the num-block location
					extreme[numIndex] = index;
					//update indeces
					numIndex += dir;
					index += (num + 1) * dir;
					if (index < 0 || index > len) {
						index -= dir;
					}
				}
				//keep going to end
				while (index >= 0 && index < len) {
					index += dir;
				}
				//go backwards and shift blocks as needed to cover 
				//any existing filled squares
				index -= dir;
				numIndex -= dir;
				while (numIndex >= 0 && numIndex < numsLen) {
					int num = nums[numIndex];
					//find a spot where the possibility doesn't cover an
					//existing filled cell
					for (int i = index; i != extreme[numIndex] + ((num - 1) * dir); i -= dir) {
						if (line[i] == 1) {
							//may need to backtrack to cover the spot
							while (!theresRoom(i, num, -dir)) {
								i += dir;
							}
							extreme[numIndex] = i + ((num - 1) * -dir);
							break;
						}
					}
					index = extreme[numIndex] - dir;
					numIndex -= dir;
				}
				//check that we did not move something off of an 
				//existing colored in cell
				while (index >= 0 && index < len) {
					if (line[index] == 1) {
						return null;
					}
					index -= dir;
				}
				
				return extreme;
			} catch(Exception e) {
				return null;
			}
		}
		
		private int[] getNewInfo(int[] left, int[] right) {
			try {
				PicrossSolverSquare[] possibilities = new PicrossSolverSquare[len];
				
				//what we know from original info
				for (int i = 0; i < len; i++) {
					possibilities[i] = new PicrossSolverSquare();
					if (line[i] == 1) {
						possibilities[i].alreadyFilled();
					} else if (line[i] == 0) {
						possibilities[i].alreadyEmpty();
					}
				}
				
				//edges outside the extremes
				for (int i = 0; i < left[0]; i++) {
					possibilities[i].cantBeFilled();
					possibilities[i].mayBeEmpty();
				}
				for (int i = right[numsLen - 1] + 1; i < len; i++) {
					possibilities[i].cantBeFilled();
					possibilities[i].mayBeEmpty();
				}
				//add possibilities to cells based on possible locations
				for (int numIndex = 0; numIndex < numsLen; numIndex++) {
					int num = nums[numIndex];
					if (left[numIndex] + num - 1 < right[numIndex] - num + 1) {
						//min and max are disjoint
						//min is ambiguous
						for (int i = left[numIndex]; i < left[numIndex] + num; i++) {
							if (line[i] == 2) {
								possibilities[i].mayBeFilled();
								possibilities[i].mayBeEmpty();
							}
						}
						//in between, mark cbfilled where there's room and cbempty where it's not filled
						for (int index = left[numIndex] + num; index <= right[numIndex] - num; index++) {
							if (line[index] == 2) {
								if (theresRoom(index, num, 1)) {
									for (int i = index; i < index + num; i++) {
										possibilities[i].mayBeFilled();
									}
								} else if (theresRoom(index, num, -1)) {
									possibilities[index].mayBeFilled();
								}
								possibilities[index].mayBeEmpty();
							}
						}
						//max is ambiguous
						for (int i = right[numIndex] - num + 1; i <= right[numIndex]; i++) {
							if (line[i] == 2) {
								possibilities[i].mayBeFilled();
								possibilities[i].mayBeEmpty();
							}
						}
					} else {
						//min and max overlap
						//write it all as ambiguous
						for (int i = left[numIndex]; i <= right[numIndex]; i++) {
							if (line[i] == 2) {
								possibilities[i].mayBeFilled();
								possibilities[i].mayBeEmpty();
							}
						}
						//rewrite the overlapping portion
						for (int i = right[numIndex] - num + 1; i <= left[numIndex] + num - 1; i++) {
							if (line[i] == 2) {
								possibilities[i].cantBeEmpty();
							}
						}
					}
				}
				for (int numIndex = 1; numIndex < numsLen; numIndex++) {
					//if possible spots for consecutive nums have them non-overlapping
					for (int i = right[numIndex - 1] + 1; i < left[numIndex]; i++) {
						possibilities[i].cantBeFilled();
						possibilities[i].mayBeEmpty();
					}
				}
				
				//return new information
				int[] newline = new int[len];
				for (int i = 0; i < len; i++) {
					Boolean cbFilled = possibilities[i].getFilled();
					Boolean cbEmpty = possibilities[i].getEmpty();
					if (Boolean.TRUE.equals(cbFilled) && Boolean.TRUE.equals(cbEmpty)) {
						//we don't know which yet
						newline[i] = 2;
					} else if (Boolean.TRUE.equals(cbFilled)) {
						//confirm fill whether Empty is deconfirmed or uninitialized
						newline[i] = 1;
					} else if (Boolean.TRUE.equals(cbEmpty)) {
						//confirm empty whether Filled is deconfirmed or uninitialized
						newline[i] = 0;
					} else if (Boolean.FALSE.equals(cbFilled) && Boolean.FALSE.equals(cbEmpty)) {
						//if both are deconfirmed then there is a contradiction in the logic
						throw new RuntimeException();
					} else if (Boolean.FALSE.equals(cbFilled)) {
						//deconfirmed Filled, while Empty uninitialized, can say empty
						newline[i] = 0;
					} else if (Boolean.FALSE.equals(cbEmpty)) {
						//deconfirmed Empty, while Filled uninitialized, can say empty
						newline[i] = 1;
					}
				}
				return newline;
				
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		//is there room for the block in the requested spot?
		@SuppressWarnings("null")
		private boolean theresRoom(int index, int num, int dir) {
			try {
				//is the prior space not filled?
				int prior = index - dir;
				if (prior != -1 && prior != len) {
					if (line[prior] == 1) {
						return false;
					}
				}
				//are all the requred spaces not crossed?
				for (int i = index; i != index + num * dir; i += dir) {
					if (line[i] == 0) {
						return false;
					}
				}
				//is the space next after not filled?
				int nextafter = index + (num * dir);
				if (nextafter != -1 && nextafter != len) {
					if (line[nextafter] == 1) {
						return false;
					}
				}
				return true;
			} catch(Exception ex) {
				return (Boolean) null;
			}
		}
		
		//tracks info on each square in the line
		class PicrossSolverSquare {
			private Boolean couldBeFilled;
			private Boolean couldBeEmpty;
			
			//we can go from true to false but not false to true
			public void alreadyFilled() {
				couldBeFilled = true;
				couldBeEmpty = false;
			}
			
			public void alreadyEmpty() {
				couldBeFilled = false;
				couldBeEmpty = true;
			}
			
			public void mayBeFilled() throws Exception {
				if (Boolean.FALSE.equals(couldBeFilled)) {
					throw new Exception();
				} else {
					couldBeFilled = true;
				}
			}
			
			public void mayBeEmpty() throws Exception {
				if (Boolean.FALSE.equals(couldBeEmpty)) {
					throw new Exception();
				} else {
					couldBeEmpty = true;
				}
			}
			
			public void cantBeFilled() {
				couldBeFilled = false;
			}
			
			public void cantBeEmpty() {
				couldBeEmpty = false;
			}
			
			public Boolean getFilled() {
				return couldBeFilled;
			}
			
			public Boolean getEmpty() {
				return couldBeEmpty;
			}
		}
	}
}