import java.util.ArrayList;
import java.util.Arrays;

public class PicrossSolver {
    private final static boolean enableDebug = true;
	private final int[][] colHeaders;
	private final int[][] rowHeaders;
	private final int numCols;
	private final int numRows;
	private final int[][] intGrid;
    private final int[][] solution;
    private final PicrossGrid picrossGrid;
    private final int emptyCell = 2;
    private final int filledCell = 1;
    private final int crossedCell = 0;
	private boolean impossible = false;

	public PicrossSolver(int[][][] headers, int[][] solution, PicrossGrid picrossGrid) {
        if (headers == null) {
            colHeaders = null;
		    rowHeaders = null;
            numCols = 0;
            numRows = 0;
            intGrid = null;
            this.solution = null;
            this.picrossGrid = null;
            runTests();
            return;
        }
		colHeaders = headers[0];
		rowHeaders = headers[1];
		numCols = colHeaders.length;
		numRows = rowHeaders.length;
		intGrid = new int[numCols][numRows];
        this.solution = solution;
        this.picrossGrid = picrossGrid;
		for (int c = 0; c < numCols; c++) {
			for (int r = 0; r < numRows; r++) {
				intGrid[c][r] = emptyCell;
			}
		}
        debug("Create solver on " + numCols + " columns and " + numRows + " rows");
	}
	
	//get whether it is solvable
	public boolean solvable() {
        debug("Checking solvability");
        iterate();

        if (impossible) {
			return false;
		}
		for (int c = 0; c < numCols; c++) {
			for (int r = 0; r < numRows; r++) {
				if (intGrid[c][r] == emptyCell) {
					return false;
				}
			}
		}
		return true;
	}
	
	//loop over all rows and columns until the loop when
	//no changes have been made
	private void iterate() {
		try {
            //get immediate easy information from all rows and cols
			boolean foundInformation = false;
            for (int c = 0; c < numCols; c++) {
                if (colUpdate(c, false)) {
                    foundInformation = true;
                }
            }
            for (int r = 0; r < numRows; r++) {
                if (rowUpdate(r, false)) {
                    foundInformation = true;
                }
            }

            //check rows and cols again, this time jumping around when something is updated
			while (foundInformation) {
				foundInformation = false;
				for (int c = 0; c < numCols; c++) {
					if (colUpdate(c, true)) {
						foundInformation = true;
					}
				}
				for (int r = 0; r < numRows; r++) {
					if (rowUpdate(r, true)) {
						foundInformation = true;
					}
				}
			}
		} catch(Exception e) {
            debug("Exiting due to impossibility: " + e);
			impossible = true;
		}
	}
	
	//look for changes to make to a column
	private boolean colUpdate(int col, boolean doPerpendiculars) throws Exception {
        debug("Try to update column " + col);
        picrossGrid.focusNums(col);
        boolean changed = false;

        //get current state
        int[] line = intGrid[col].clone();

        //determine any new info
        int[] newline = (new PicrossSolverLine(line, colHeaders[col])).getNewLine();

        //post new info and call the perpendiculars
        boolean[] rowsToUpdate = new boolean[numRows];
        Arrays.fill(rowsToUpdate, false);
        for (int r = 0; r < numRows; r++) {
            if (newline[r] != line[r]) {
                if (line[r] == emptyCell) {
                    if (enableDebug && solution != null && 
                        ((newline[r] == crossedCell && solution[col][r] != 0) || (newline[r] == filledCell && solution[col][r] != 1))) {
                        //we got something wrong
                        debug("Throwing exception. Got cell " + r + " on column " + col + " wrong. Line: " + Arrays.toString(line) + 
                            " Nums: " + Arrays.toString(colHeaders[col]) + " Newline: " + Arrays.toString(newline));
                        throw new Exception();
                    }
                    intGrid[col][r] = newline[r];
                    changed = true;
                    rowsToUpdate[r] = true;

                    if (!enableDebug) {
                        Thread.sleep(20);
                    }
                    if (newline[r] == filledCell) {
                        picrossGrid.fillCell(col, r);
                    } else {
                        picrossGrid.clearCell(col, r);
                    }
                } else {
                    //should not be rewriting or erasing
                    debug("Throwing exception. Tried to overwrite cell " + r + " of column " + col);
                    throw new Exception();
                }
            }
        }

        picrossGrid.unfocusNums(col);
        if (doPerpendiculars) {
            debug("Will update rows due to changes on column " + col + ": " + Arrays.toString(rowsToUpdate));
            for (int r = 0; r < numRows; r++) {
                if (rowsToUpdate[r]) {
                    rowUpdate(r, true);
                }
            }
        }

        return changed;
	}
	
	//look for changes to make to a row
	private boolean rowUpdate(int row, boolean doPerpendiculars) throws Exception {
        debug("Try to update row " + row);
        picrossGrid.focusNums(numCols + row);
        boolean changed = false;

        //get current state
        int[] line = new int[numCols];
        for (int c = 0; c < numCols; c++) {
            line[c] = intGrid[c][row];
        }

        //determine any new info
        int[] newline = (new PicrossSolverLine(line, rowHeaders[row])).getNewLine();

        //post new info and call the perpendiculars
        boolean[] colsToUpdate = new boolean[numCols];
        Arrays.fill(colsToUpdate, false);
        for (int c = 0; c < numCols; c++) {
            if (newline[c] != line[c]) {
                if (line[c] == emptyCell) {
                    if (enableDebug && solution != null && 
                        ((newline[c] == crossedCell && solution[c][row] != 0) || (newline[c] == filledCell && solution[c][row] != 1))) {
                        //we got something wrong
                        debug("Throwing exception. Got cell " + c + " on row " + row + " wrong. Line: " + Arrays.toString(line) + 
                            " Nums: " + Arrays.toString(rowHeaders[row]) + " Newline: " + Arrays.toString(newline));
                        throw new Exception();
                    }
                    intGrid[c][row] = newline[c];
                    changed = true;
                    colsToUpdate[c] = true;
                    
                    if (!enableDebug) {
                        Thread.sleep(20);
                    }
                    if (newline[c] == filledCell) {
                        picrossGrid.fillCell(c, row);
                    } else {
                        picrossGrid.clearCell(c, row);
                    }
                } else {
                    //should not be rewriting or erasing
                    debug("Throwing exception. Tried to overwrite cell " + c + " of row " + row);
                    throw new Exception();
                }	
            } 
        }

        picrossGrid.unfocusNums(numCols + row);
        if (doPerpendiculars) {
            debug("Will update cols due to changes on row " + row + ": " + Arrays.toString(colsToUpdate));
            for (int c = 0; c < numCols; c++) {
                if (colsToUpdate[c]) {
                    colUpdate(c, true);
                }
            }
        }

        return changed;
	}
	
	//used to glean any new info on a line
	class PicrossSolverLine {
		private final int[] line;
		private final int[] nums;
		private final int len;
		private final int countNums;
        private PicrossSolverLineSection[] sections;
        private int countSections;
        private int[] distribution;
        private boolean empty = false;

		public PicrossSolverLine(int[] line, int[] nums) {
            this.line = line;
			this.nums = nums;
			len = line.length;
			countNums = nums.length;
            debug("Given line " + Arrays.toString(line) + " and nums " + Arrays.toString(nums));
            if (nums[0] == 0) {
                empty = true;
                debug("Line is confimed crossed out");
                return;
            }

            //isolate and collect the sections of the line that are not crossed out
            int left = 0;
            int index = 0;
            ArrayList<PicrossSolverLineSection> sectionsList = new ArrayList<>();
            while (index < len) {
                if (line[index] == crossedCell) {
                    index++;
                } else {
                    left = index;
                    while (index < len && line[index] != crossedCell) {
                        index++;
                    }
                    debug("Found section: " + Arrays.toString(Arrays.copyOfRange(line, left, index)));
                    sectionsList.add(new PicrossSolverLineSection(Arrays.copyOfRange(line, left, index)));
                }
            }
            sections = sectionsList.toArray(new PicrossSolverLineSection[sectionsList.size()]);

            //distribute the numbers among the sections
            //when there is a distribution that works, collect information on possible exact locations
            countSections = sections.length;
            distribution = new int[countNums];
            
            debug("Found " + countSections + " sections total");
		}

        public int[] getNewLine() throws Exception {
            if (empty) {
                int[] newLine = new int[line.length];
                Arrays.fill(newLine, crossedCell);
                debug("Returning line " + Arrays.toString(newLine));
                return newLine;
            }

            tryEveryDistribution(0, 0, 0);
            
            int[] newLine = line.clone();
            int index = 0;
            int sectionIndex = 0;

            while (index < len) {
                if (newLine[index] == crossedCell) {
                    index++;
                } else {
                    int[] newSection = sections[sectionIndex].getNewSectionInfo();
                    for (int i = 0; i < newSection.length; i++) {
                        newLine[index] = newSection[i];
                        index++;
                    }
                    sectionIndex++;
                }
            }
            
            debug("Returning line " + Arrays.toString(newLine));
            return newLine;
        }

        //fill an int array with the section index of each number, making sure that each is at least as far down
        //as the last, but allowing for sections to be skipped
        private void tryEveryDistribution(int numIndex, int sectionIndex, int spacesTaken) throws Exception {
            if (sections[sectionIndex].len - spacesTaken >= nums[numIndex]) {
                distribution[numIndex] = sectionIndex;
                if (numIndex == countNums - 1) {
                    tryDistribution();
                } else {
                    tryEveryDistribution(numIndex + 1, sectionIndex, spacesTaken + nums[numIndex] + 1);
                }
            }

            for (int i = sectionIndex + 1; i < countSections; i++) {
                if (sections[i].len >= nums[numIndex]) {
                    distribution[numIndex] = i;
                    if (numIndex == countNums - 1) {
                        tryDistribution();
                    } else {
                        tryEveryDistribution(numIndex + 1, i, nums[numIndex] + 1);
                    }
                }
            }
        }

        //try the distribution and if there are no problems, see what new information it can give
        private void tryDistribution() throws Exception {
            debug("Trying distribution " + Arrays.toString(distribution));
            int previousSectionIndex = -1;
            int leftNumsIndex = 0;
            int rightNumsIndex = 1;
            while (rightNumsIndex <= nums.length) {
                int sectionIndex = distribution[leftNumsIndex]; 

                //break in between assigned sections
                for (int i = previousSectionIndex + 1; i < sectionIndex; i++) {
                    if (!sections[i].possibleToFit(new int[] {0})) {
                        return;
                    }
                }

                //stretch of nums assigned to same section
                while (rightNumsIndex < nums.length && distribution[rightNumsIndex] == sectionIndex) {
                    rightNumsIndex++;
                }
                if (!sections[sectionIndex].possibleToFit(Arrays.copyOfRange(nums, leftNumsIndex, rightNumsIndex))) {
                    return;
                }

                previousSectionIndex = sectionIndex;
                leftNumsIndex = rightNumsIndex;
                rightNumsIndex++;
            }
            for (int i = previousSectionIndex + 1; i < sections.length; i++) {
                if (!sections[i].possibleToFit(new int[] {0})) {
                    return;
                }
            }
            
            debug("Distribution passed");
            for (PicrossSolverLineSection section : sections) {
                section.collectPossiblePlacementInfo();
                section.collectionDebugMessage();
            }
        }

        //represents a stretch of boxes that are not crossed out in between two boxes that are
        //rely on the nature of wrapper class Boolean to be default Null, collect information on whether boxes may or may not be filled
        private class PicrossSolverLineSection {
            public int[] section;
            public int len;
            public int[] nums;
            public int[] positions;
            private int numsLen;
            private enum Possibility { YES, NO, UNKNOWN }
            private Possibility[] couldBeCrossed;
            private Possibility[] couldBeFilled;
            private boolean hasSomeFilled;

            public PicrossSolverLineSection(int[] section) {
                this.section = section;
                len = section.length;
                couldBeCrossed = new Possibility[len];
                Arrays.fill(couldBeCrossed, Possibility.UNKNOWN);
                couldBeFilled = couldBeCrossed.clone();

                for (int i = 0; i < len; i++) {
                    if (section[i] == filledCell) {
                        couldBeCrossed[i] = Possibility.NO;
                        couldBeFilled[i] = Possibility.YES;
                        hasSomeFilled = true;
                    }
                }
            }

            //see if the given numbers will fit at all
            public boolean possibleToFit(int[] nums) {
                debug("Try to fit " + Arrays.toString(nums) + " in " + Arrays.toString(section));
                try {
                    this.nums = nums;
                    numsLen = nums.length;
                    positions = new int[numsLen];
                    if (numsLen == 0 || nums[0] == 0) {
                        debug("Are there already filled in spaces? " + hasSomeFilled);
                        return !hasSomeFilled;
                    }

                    if (nums[0] == len) { 
                        if (numsLen == 1) {
                            debug("Fits exactly");
                            return true;
                        } else {
                            debug("Obviously too large");
                            return false;
                        }
                    }

                    //establish starting positions not considering filled in spaces
                    int index = 0;
                    int numIndex = 0;
                    while (numIndex < numsLen - 1) {
                        positions[numIndex] = index;
                        index += nums[numIndex] + 1;
                        numIndex++;
                    }
                    positions[numIndex] = index;

                    return couldFindNextAcceptablePositions(numsLen - 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    debug("Went over the end of the line, couldn't fit them");
                    return false;
                }
            }

            //if there are any uncovered spaces, move each over just enough.
            //start loop over if anything is moved in case we uncovered a space again
            private boolean couldFindNextAcceptablePositions(int numIndex) throws ArrayIndexOutOfBoundsException {
                int index = len - 1;
                if (numIndex < numsLen - 1) {
                    index = positions[numIndex + 1] - 1;
                }

                int[] newPositions = positions.clone();
                boolean loopAgain = true;
                if (numIndex == -1) {
                    loopAgain = false;
                }
                while (loopAgain) {
                    loopAgain = false;
                    int n = numIndex;
                    int i = index;

                    while (!loopAgain && n >= 0) {
                        while (!loopAgain && i >= newPositions[n] + nums[n]) {
                            if (section[i] == filledCell) {
                                debug("Jump num at num index " + n + " to index " + (i - nums[n] + 1));
                                newPositions[n] = i - nums[n] + 1;
                                loopAgain = true;
                            }
                            i--;
                        }
                        moveIfPositionIsUnacceptable(n, newPositions);
                        i = newPositions[n] - 1;
                        n--;
                    }

                    while (!loopAgain && i >= 0) {
                        if (section[i] == filledCell) {
                            return false;
                        }
                        i--;
                    }
                }
                for (int i = 0; i < newPositions[0]; i++) {
                    if (section[i] == filledCell) {
                        return false;
                    }
                }
                debug("Found positions " + Arrays.toString(positions));
                positions = newPositions;
                return true;
            }

            private void moveIfPositionIsUnacceptable(int numIndex, int[] newPositions) throws ArrayIndexOutOfBoundsException {
                debug("Check if we need to move num at num index " + numIndex + ": " + Arrays.toString(newPositions));
                //if space on either side is filled, need to keep moving
                //don't move if it would make us touch the neighbor
                if (newPositions[numIndex] + nums[numIndex] < len && 
                    ((newPositions[numIndex] - 1 != -1 ? section[newPositions[numIndex] - 1] == filledCell : false) || 
                    section[newPositions[numIndex] + nums[numIndex]] == filledCell)) {
                    int nextNumIndex = numIndex;
                    while (nextNumIndex < numsLen - 1 && newPositions[nextNumIndex] + nums[nextNumIndex] + 1 == newPositions[nextNumIndex + 1]) {
                        nextNumIndex++;
                    }
                    while (nextNumIndex > numIndex) {
                        newPositions[nextNumIndex]++;
                        if (newPositions[nextNumIndex] + nums[nextNumIndex] > len) {
                            throw new ArrayIndexOutOfBoundsException();
                        }
                        debug("Move over num at num index " + nextNumIndex + " to make room for " + numIndex);
                        moveIfPositionIsUnacceptable(nextNumIndex, newPositions);
                        nextNumIndex--;
                    }
                    debug("Move over num at num index " + numIndex);
                    newPositions[numIndex]++;
                    moveIfPositionIsUnacceptable(numIndex, newPositions);
                }
            }

            //positions have been verified to be ok, so fill out couldBe arrays 
            public void collectPossiblePlacementInfo() throws Exception {
                debug("Collect info for " + Arrays.toString(section));
                if (nums[0] == 0) {
                    for (int i = 0; i < couldBeCrossed.length; i++) {
                        setCouldBeCrossed(i);
                    }
                    return;
                }
                if (nums[0] == len) {
                    for (int i = 0; i < couldBeFilled.length; i++) {
                        couldBeFilled[i] = Possibility.YES;
                    }
                    return;
                }

                //info from positions as-is, plus moving them all over without moving them off filled cells
                int index = 0;
                int numIndex = 0;
                boolean[] covering = new boolean[numsLen];
                while (numIndex < numsLen) {
                    while (index < positions[numIndex]) {
                        setCouldBeCrossed(index);
                        index++;
                    }
                    while (index < positions[numIndex] + nums[numIndex]) {
                        if (section[index] == filledCell) {
                            covering[numIndex] = true;
                        }
                        couldBeFilled[index] = Possibility.YES;
                        index++;
                    }
                    numIndex++;
                }
                while (index < len) {
                    setCouldBeCrossed(index);
                    index++;
                }
                for (int i = numsLen - 1; i > 0; i--) {
                    maybeMovePlacementOver(i);
                }
                
                //move to the right in a sweeping motion so that we catch possibilities where things are squished towards center
                //and then possibilities where things are pushed far to the right
                numIndex = 0;
                int[] oldPositions = positions;
                while (numIndex < numsLen) {
                    debug("See if nums up to num index " + numIndex + " can be shifted");
                    oldPositions = positions.clone();
                    if (couldMoveWithoutCollision(numIndex)) {
                        try {
                            if (couldFindNextAcceptablePositions(numIndex)) {
                                for (int n = numIndex; n > 0; n--) {
                                    if (positions[n] != oldPositions[n]) {
                                        for (int i = positions[n - 1] + nums[n - 1]; i < positions[n]; i++) {
                                            setCouldBeCrossed(i);
                                        }
                                        for (int i = positions[n]; i < positions[n] + nums[n]; i++) {
                                            couldBeFilled[i] = Possibility.YES;
                                        }
                                        maybeMovePlacementOver(n);
                                    }
                                }
                                if (positions[0] != oldPositions[0]) {
                                    for (int i = oldPositions[0]; i < positions[0]; i++) {
                                        setCouldBeCrossed(i);
                                    }
                                    for (int i = positions[0]; i < positions[0] + nums[0]; i++) {
                                        couldBeFilled[i] = Possibility.YES;
                                    }
                                    maybeMovePlacementOver(0);
                                }
                                numIndex = -1;
                            } else {
                                positions = oldPositions;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            return;
                        }
                    } else {
                        positions = oldPositions;
                    }
                    numIndex++;


                    /*if (couldMoveWithoutCollision(firstMovable)) {
                        try {
                            if (!couldFindNextAcceptablePositions(lastIndex, lastMovable)) {
                                return;
                            }
                            for (int n = lastMovable; n > 0; n--) {
                                for (int i = positions[n - 1] + nums[n - 1]; i < positions[n]; i++) {
                                    setCouldBeCrossed(i);
                                }
                                for (int i = positions[n]; i < positions[n] + nums[n]; i++) {
                                    couldBeFilled[i] = Possibility.YES;
                                }
                            }
                            for (int i = positions[0]; i < positions[0] + nums[0]; i++) {
                                couldBeFilled[i] = Possibility.YES;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            return;
                        }
                        maybeMovePlacementOver(lastMovable);
                        lastIndex = positions[lastMovable] - 1;
                    } else {
                        lastIndex = positions[lastMovable] - 1;
                        firstMovable++;
                    }*/
                }
                
                
                
                /*for (int i = 0; i < positions[0]; i++) {
                    setCouldBeCrossed(i);
                }
                maybeMovePlacementOver(lastMovable);*/
            }

            public void collectionDebugMessage() {
                debug("Collected info for section " + Arrays.toString(section) + ": couldBeCrossed: " + Arrays.toString(couldBeCrossed) +
                     " and couldBeFilled: " + Arrays.toString(couldBeFilled));
            }

            private void maybeMovePlacementOver(int numIndex) throws Exception {
                while (canMovePlacementOverByOne(numIndex)) {
                    debug("Move num index " + numIndex + " to " + (positions[numIndex] + 1));
                    setCouldBeCrossed(positions[numIndex]);
                    couldBeFilled[positions[numIndex] + nums[numIndex]] = Possibility.YES;
                    positions[numIndex]++;
                }
            }

            private boolean canMovePlacementOverByOne(int numIndex) {
                //debug("Check if can move " + numIndex + "th number");
                if (section[positions[numIndex]] == emptyCell) {
                    try {
                        return section[positions[numIndex] + nums[numIndex] + 1] == emptyCell &&
                            positions[numIndex] + nums[numIndex] + 1 != positions[numIndex + 1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return positions[numIndex] + nums[numIndex] < len;
                    }
                } 
                return false;
            }

            private boolean couldMoveWithoutCollision(int numIndex) {
                debug("Try to force num index " + numIndex);
                int newPosition = positions[numIndex];
                do {
                    newPosition++;
                } while (newPosition + nums[numIndex] >= len ? false : 
                (section[newPosition - 1] == filledCell || section[newPosition + nums[numIndex]] == filledCell));
                
                if (section[newPosition - 1] == emptyCell && 
                    newPosition + nums[numIndex] < (numIndex == numsLen - 1 ? len + 1 : positions[numIndex + 1])) {
                    positions[numIndex] = newPosition;
                    return true;
                } else {
                    return false;
                }
            }

            private void setCouldBeCrossed(int index) throws Exception {
                if (index == -1 || index == len) {
                    return;
                }
                if (couldBeCrossed[index] == Possibility.NO) {
                    debug("Tried to overwrite uncrossed cell " + index + " of " + Arrays.toString(section));
                    throw new Exception();
                } else {
                    couldBeCrossed[index] = Possibility.YES;
                }
            }

            public int[] getNewSectionInfo() {
                int[] newSection = section.clone();
                for (int i = 0; i < section.length; i++) {
                    if (couldBeFilled[i] == Possibility.YES && couldBeCrossed[i] != Possibility.YES) {
                        newSection[i] = filledCell;
                    } else if (couldBeFilled[i] != Possibility.YES && couldBeCrossed[i] == Possibility.YES) {
                        newSection[i] = crossedCell;
                    }
                }
                return newSection;
            }
        }
	}

    private void debug(String message) {
        if (enableDebug) {
            System.out.println(message);
        }
    }

    public static void runTestSuite() {
        if (enableDebug) {
            new PicrossSolver(null, null, null);
        } 
    }

    private void runTests() {
        if (!enableDebug) {
            return;
        }

        int index = 1;
        boolean noFailure = true;
        for (String testResult : new String[] {
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {0},               new int[] {0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {15},              new int[] {1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {1,1,1,1,1,1,1,1}, new int[] {1,0,1,0,1, 0,1,0,1,0, 1,0,1,0,1}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {7},               new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {8},               new int[] {2,2,2,2,2, 2,2,1,2,2, 2,2,2,2,2}),

            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {10},              new int[] {2,2,2,2,2, 1,1,1,1,1, 2,2,2,2,2}),
            test(index++, new int[] {2,1,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {10},              new int[] {2,1,1,1,1, 1,1,1,1,1, 2,0,0,0,0}),
            test(index++, new int[] {2,2,1,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {5},               new int[] {2,2,1,1,1, 2,2,0,0,0, 0,0,0,0,0}),
            test(index++, new int[] {2,2,1,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {5,1},             new int[] {2,2,1,1,1, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {5,5},             new int[] {2,2,2,2,1, 2,2,2,2,2, 1,2,2,2,2}), //10

            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {4,4,4},           new int[] {2,1,1,1,2, 2,1,1,1,2, 2,1,1,1,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {3,6,4},           new int[] {1,1,1,0,1, 1,1,1,1,1, 0,1,1,1,1}),
            test(index++, new int[] {2,2,1,2,2, 2,1,2,2,2, 2,1,2,2,2}, new int[] {3,6,4},           new int[] {1,1,1,0,1, 1,1,1,1,1, 0,1,1,1,1}),
            test(index++, new int[] {2,2,2,2,0, 2,2,2,2,2, 2,2,2,2,2}, new int[] {8},               new int[] {0,0,0,0,0, 2,2,1,1,1, 1,1,1,2,2}),
            test(index++, new int[] {2,2,2,2,0, 2,2,2,2,2, 2,2,2,2,2}, new int[] {5},               new int[] {0,0,0,0,0, 2,2,2,2,2, 2,2,2,2,2}),

            test(index++, new int[] {2,2,0,2,2, 0,2,2,0,2, 2,0,2,2,2}, new int[] {2,2,1,2},         new int[] {2,2,0,2,2, 0,2,2,0,2, 2,0,2,2,2}),
            test(index++, new int[] {2,2,0,2,2, 0,2,2,0,2, 2,0,2,2,2}, new int[] {2,2,1,3},         new int[] {2,2,0,2,2, 0,2,2,0,2, 2,0,1,1,1}),
            test(index++, new int[] {2,2,0,2,2, 0,2,2,0,2, 2,0,2,2,2}, new int[] {2,2,1,2,2},       new int[] {1,1,0,1,1, 0,2,2,0,1, 1,0,2,1,2}),
            test(index++, new int[] {2,2,2,2,2, 0,2,2,2,2, 2,2,2,2,2}, new int[] {3,5},             new int[] {2,2,2,2,2, 0,2,2,2,2, 1,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,0,2,2, 2,2,2,2,2}, new int[] {7},               new int[] {2,2,2,2,2, 2,2,0,2,2, 2,2,2,2,2}), //20

            test(index++, new int[] {2,2,2,2,2, 2,2,0,2,2, 2,1,2,2,2}, new int[] {7},               new int[] {0,0,0,0,0, 0,0,0,1,1, 1,1,1,1,1}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {1,1,1,1,1,1,1},   new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,1,2}, new int[] {1,1,1,1,1,1,1},   new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,0,1,0}),
            test(index++, new int[] {0,2,2,2,2, 2,2,2,2,2, 2,2,2,2,0}, new int[] {1,1,1,1,1,1,1},   new int[] {0,1,0,1,0, 1,0,1,0,1, 0,1,0,1,0}),
            test(index++, new int[] {2,2,2,2,2, 0,1,2,2,2, 2,2,2,2,2}, new int[] {3,4},             new int[] {2,2,2,2,2, 0,1,1,1,2, 2,2,2,2,2}),
            
            test(index++, new int[] {1,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {4,1},             new int[] {1,1,1,1,0, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,1}, new int[] {1,4},             new int[] {2,2,2,2,2, 2,2,2,2,2, 0,1,1,1,1}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,0,1, 0,2,2,2,2}, new int[] {1,3},             new int[] {0,0,0,0,0, 0,0,0,0,1, 0,2,1,1,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,1, 1,1,2,2,2}, new int[] {5,2},             new int[] {0,0,0,0,0, 0,0,1,1,1, 1,1,0,1,1}),
            test(index++, new int[] {2,2,2,2,2, 2,2,1,1,2, 2,2,2,2,2}, new int[] {1,5,1},           new int[] {2,2,2,2,2, 2,2,1,1,2, 2,2,2,2,2}), //30
            
            test(index++, new int[] {2,2,2,2,2, 1,2,2,2,2, 2,2,2,2,2}, new int[] {5,3},             new int[] {0,2,2,2,2, 1,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,0, 1,2,2,2,2, 2,2,2,2,2}, new int[] {3,3},             new int[] {2,2,2,2,0, 1,1,1,0,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 0,1,2,2,2}, new int[] {3,3},             new int[] {2,2,2,2,2, 2,2,2,2,2, 0,1,1,1,0}),
            test(index++, new int[] {0,1,1,1,0, 2,2,1,2,2, 2,2,1,2,2}, new int[] {3,3,3},           new int[] {0,1,1,1,0, 2,2,1,2,2, 2,2,1,2,2}),
            test(index++, new int[] {0,1,1,1,0, 2,2,2,2,2, 2,2,2,2,2}, new int[] {3,3,3},           new int[] {0,1,1,1,0, 2,2,2,2,2, 2,2,2,2,2}),
            
            test(index++, new int[] {2,2,2,2,2, 1,2,2,2,2, 2,2,2,2,2}, new int[] {5},               new int[] {0,2,2,2,2, 1,2,2,2,2, 0,0,0,0,0}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {1},               new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,0,2,2, 0,0,2,2,2, 0,2,0,2,2}, new int[] {3,1},             new int[] {0,0,0,0,0, 0,0,1,1,1, 0,2,0,2,2}),
            test(index++, new int[] {2,2,1,1,1, 2,2,2,2,2, 1,1,2,2,2}, new int[] {1,3,4},           new int[] {1,0,1,1,1, 0,0,0,2,2, 1,1,2,2,0}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {14},              new int[] {2,1,1,1,1, 1,1,1,1,1, 1,1,1,1,2}), //40

            test(index++, new int[] {0,2,2,2,2, 2,2,2,1,2, 2,2,2,2,0}, new int[] {1,4,1},           new int[] {0,2,2,2,2, 2,2,2,1,2, 2,2,2,2,0}),
            test(index++, new int[] {0,2,2,2,2, 2,2,1,0,2, 2,2,2,2,0}, new int[] {1,2,1,1,1},       new int[] {0,2,2,2,2, 2,2,1,0,2, 2,2,2,2,0}),
            test(index++, new int[] {0,2,2,2,2, 1,2,2,2,1, 0,2,2,2,0}, new int[] {1,2,3,1},         new int[] {0,2,2,0,1, 1,0,1,1,1, 0,2,2,2,0}),
            test(index++, new int[] {2,2,2,1,1, 1,2,2,2,2, 1,1,2,2,2}, new int[] {1,3,4},           new int[] {2,2,0,1,1, 1,0,0,2,2, 1,1,2,2,0}),
            test(index++, new int[] {2,2,2,2,0, 0,2,2,1,2, 2,2,2,2,2, 1,2,2,2,2}, new int[] {4,1,2,5},    new int[] {1,1,1,1,0, 0,2,0,1,2, 2,2,2,2,2, 1,2,2,2,2}),

            test(index++, new int[] {1,1,1,1,0, 2,2,2,2,2, 2,1,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {4,5,3}, 
                          new int[] {1,1,1,1,0, 2,2,2,2,2, 2,1,2,2,2, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {2,2,2,2,1, 1,2,2,2,2, 2,1,1,2,2, 2,2,2,1,1, 1,2,2,2,2}, new int[] {4,1,3,5,3}, 
                          new int[] {0,0,2,2,1, 1,2,2,2,2, 2,1,1,2,0, 0,1,1,1,1, 1,0,1,1,1}),
            test(index++, new int[] {2,2,1,2,0, 2,2,2,2,2, 0,2,2,0,2, 2,2,2,0,2, 2,2,2,2,2}, new int[] {2,1,3}, 
                          new int[] {0,2,1,2,0, 2,2,2,2,2, 0,2,2,0,2, 2,2,2,0,2, 2,2,2,2,2}),
            test(index++, new int[] {1,1,0,0,0, 0,2,1,1,1, 2,2,1,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {2,4,1,1,2,2,1}, 
                          new int[] {1,1,0,0,0, 0,2,1,1,1, 2,0,1,0,2, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {0,0,0,1,2, 2,2,2,2,2, 2,2,2,2,1, 1,0,2,2,2, 2,0,2,2,2}, new int[] {2,2,1,2}, 
                          new int[] {0,0,0,1,1, 0,2,2,2,2, 2,2,2,0,1, 1,0,2,2,2, 2,0,2,2,2}), //50
            
            test(index++, new int[] {0,0,1,1,0, 1,0,0,2,2, 2,2,2,2,1, 2,2,2,0,0}, new int[] {2,1,1,1,1,2},      
                          new int[] {0,0,1,1,0, 1,0,0,2,2, 2,2,2,0,1, 2,2,2,0,0}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,2,1, 2,2,1,2,2, 2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}, new int[] {5,1,4,4},      
                          new int[] {2,2,2,2,2, 2,2,2,2,1, 2,2,1,2,2, 2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2, 2,2,2,2,2}),
            test(index++, new int[] {0,0,0,0,0, 0,0,0,0,0, 0,0,2,2,2, 2,2,2,2,2, 0,0,2,2,2, 0,2,2,2,0, 2,1,2,2,2, 2,0,0,1,0}, new int[] {1,2,1,1,2,1},      
                          new int[] {0,0,0,0,0, 0,0,0,0,0, 0,0,2,2,2, 2,2,2,2,2, 0,0,2,2,2, 0,2,2,2,0, 2,1,2,2,2, 2,0,0,1,0}),
            test(index++, new int[] {2,2,2,2,2, 2,2,2,1,2, 2,2,2,2,2, 2,2,1,2,2, 0,1,0,2,1}, new int[] {2,2,1,1,2}, 
                          new int[] {2,2,2,2,2, 2,2,2,1,2, 2,2,2,2,2, 2,2,1,0,2, 0,1,0,1,1})
        }) {
            if (testResult.length() > 0) {
                System.out.println(testResult);
                noFailure = false;
            }
        }
        if (noFailure) {
            System.out.println("\nAll test cases pass!");
        }
    }

    private String test(int testIndex, int[] inputline, int[] inputnums, int[] output) {
        debug("Test " + testIndex);
        return test(testIndex, inputline, inputnums, output, false);
    }

    private String test(int testIndex, int[] inputline, int[] inputnums, int[] output, boolean expectFail) {
        int[] result = new int[0];
        try {
            result = (new PicrossSolverLine(inputline, inputnums)).getNewLine();
            int i = 0;
            while (i < result.length) {
                if (result[i] != output[i]) {
                    return "Test " + testIndex + " failure: Expected: " + Arrays.toString(output) + " Result: " + Arrays.toString(result);
                }
                i++;
            }
            if (i < output.length) {
                if (expectFail) {
                    return "";
                }
                return "Test " + testIndex + " failure: Expected: " + Arrays.toString(output) + " Result: " + Arrays.toString(result);
            }
            return "";
        } catch (Exception e) {
            if (expectFail) {
                return "";
            }
            return "Test " + testIndex + " failure: Expected: " + Arrays.toString(output) + " Result: " + Arrays.toString(result) + " Exception: " + e;
        }
    }
}