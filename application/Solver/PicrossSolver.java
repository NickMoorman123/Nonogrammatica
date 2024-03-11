package Solver;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class PicrossSolver {
    public final static boolean inDebugMode = false;
	private final int[][] rowHeaders;
	private final int[][] colHeaders;
	private final int numRows;
	private final int numCols;
	private final int[][] intGrid;
    private final Optional<int[][]> solution;
    public final static int EMPTY_CELL = 2;
    public final static int FILLED_CELL = 1;
    public final static int CROSSED_CELL = 0;
    private ConcurrentLinkedQueue<SolutionDisplayStep> solutionDisplaySteps = new ConcurrentLinkedQueue<SolutionDisplayStep>();
    private boolean foundInformation = false;
	private boolean impossible = false;

	public PicrossSolver(int[][] rowHeaders, int[][] colHeaders, Optional<int[][]> solution) {
		this.rowHeaders = rowHeaders;
		this.colHeaders = colHeaders;
		numRows = rowHeaders.length;
		numCols = colHeaders.length;
		intGrid = new int[numRows][numCols];
        this.solution = solution;

        for (int[] row : intGrid) {
            Arrays.fill(row, EMPTY_CELL);
        }
        debug("Create solver on " + numRows + " rows and " + numCols + " cols");
	}
	
	public boolean solvable() {
        debug("Checking solvability");
        solve();

        if (impossible) {
			return false;
		}
        return !Arrays.stream(intGrid)
                      .anyMatch(row -> Arrays.stream(row)
                      .anyMatch(cell -> cell == EMPTY_CELL));
	}

    public ConcurrentLinkedQueue<SolutionDisplayStep> getSolutionSteps() {
        return solutionDisplaySteps;
    }
	
	private void solve() {
		try {
            IntStream.range(0, numRows)
                     .iterator()
                     .forEachRemaining((IntConsumer) this::updateRow);
            IntStream.range(0, numCols)
                     .iterator()
                     .forEachRemaining((IntConsumer) this::updateCol);

			while (foundInformation) {
				foundInformation = false;
                IntStream.range(0, numRows)
                         .iterator()
                         .forEachRemaining((IntConsumer) this::updateRowDoPerpendicularJumps);
				IntStream.range(0, numCols)
                         .iterator()
                         .forEachRemaining((IntConsumer) this::updateColDoPerpendicularJumps);
			}
		} catch(Exception e) {
            debug("Exiting due to impossibility: " + e);
			impossible = true;
		}
	}
	
	private boolean[] updateRow(int row) throws RuntimeException {
        debug("Try to update row " + row);
        int[] line = intGrid[row].clone(); //clone() is ok because these are primitives
        int[] newLine = PicrossLineSolver.tryToSolveLine(line, rowHeaders[row]);

        boolean[] colsToUpdate = new boolean[numCols];
        IntStream.range(0, numCols)
                 .iterator()
                 .forEachRemaining((IntConsumer) col -> updateCell(row, col, col, line, newLine, colsToUpdate, false));

        return colsToUpdate;
	}
	
	private boolean[] updateCol(int col) throws RuntimeException {
        debug("Try to update column " + col);
        int[] line = IntStream.range(0, numRows)
                              .map(row -> intGrid[row][col])
                              .toArray();
        int[] newLine = PicrossLineSolver.tryToSolveLine(line, colHeaders[col]);

        boolean[] rowsToUpdate = new boolean[numRows];
        IntStream.range(0, numRows)
                 .iterator()
                 .forEachRemaining((IntConsumer) row -> updateCell(row, col, row, line, newLine, rowsToUpdate, true));
            
        return rowsToUpdate;
	}

    private void updateCell(int row, int col, int index, int[] line, int[] newLine, boolean[] toUpdate, boolean isColumn) {
        if (newLine[index] == line[index]) {
            return;
        }

        if (line[index] != EMPTY_CELL) {
            debug("Throwing exception. Tried to overwrite cell " + index);
            throw new RuntimeException();
        }

        if (solution.isPresent() && ((newLine[index] == CROSSED_CELL && solution.get()[row][col] != CROSSED_CELL) || (newLine[index] == FILLED_CELL && solution.get()[row][col] != FILLED_CELL))) {
            debug("Throwing exception. Got cell " + index + " wrong. Line: " + Arrays.toString(line));
            throw new RuntimeException();
        }
        intGrid[row][col] = newLine[index];
        toUpdate[index] = true;
        foundInformation = true;
        
        solutionDisplaySteps.add(new SolutionDisplayStep(row, col, newLine[index], isColumn));
    }

    private void updateRowDoPerpendicularJumps(int row) throws RuntimeException {
        var colsToUpdate = updateRow(row);

        debug("Will update cols due to changes on row " + row + ": " + Arrays.toString(colsToUpdate));
        IntStream.range(0, numCols)
                 .filter(col -> colsToUpdate[col])
                 .iterator()
                 .forEachRemaining((IntConsumer) this::updateColDoPerpendicularJumps);
    }

    private void updateColDoPerpendicularJumps(int col) throws RuntimeException {
        boolean[] rowsToUpdate = updateCol(col);

        debug("Will update rows due to changes on column " + col + ": " + Arrays.toString(rowsToUpdate));
        IntStream.range(0, numRows)
                 .filter(row -> rowsToUpdate[row])
                 .iterator()
                 .forEachRemaining((IntConsumer) this::updateRowDoPerpendicularJumps);
    }

    protected static void debug(String message) {
        if (inDebugMode) {
            System.out.println(message);
        }
    }

    public static void maybeRunTests() {
        if (!inDebugMode) {
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
                          new int[] {2,2,2,2,2, 2,2,2,1,2, 2,2,2,2,2, 2,2,1,0,2, 0,1,0,1,1}),

            test(index++, new int[] {0,1,0,1,1, 0,0,1,1,1, 0,2,2,0,0, 1,1,0,0,1}, new int[] {1,2,3,2,1}, new int[] {0,1,0,1,1, 0,0,1,1,1, 0,0,0,0,0, 1,1,0,0,1}),
            exceptionTest(index++, new int[] {2,1,2,1,2}, new int[] {1}, new int[] {}),
            exceptionTest(index++, new int[] {0,1,0,1,0}, new int[] {1}, new int[] {}),
            test(index++, new int[] {1,2,2,0,2, 1,0,0,0,0, 0,1,2,2,1}, new int[] {1,1,1,1},         new int[] {1,0,0,0,0, 1,0,0,0,0, 0,1,0,0,1})
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

    private static String test(int testIndex, int[] inputline, int[] inputnums, int[] output) {
        debug("Test " + testIndex);
        return test(testIndex, inputline, inputnums, output, false);
    }

    private static String exceptionTest(int testIndex, int[] inputline, int[] inputnums, int[] output) {
        debug("Test " + testIndex);
        return test(testIndex, inputline, inputnums, output, true);
    }

    private static String test(int testIndex, int[] inputline, int[] inputnums, int[] output, boolean expectFail) {
        int[] result = new int[0];
        try {
            result = PicrossLineSolver.tryToSolveLine(inputline, inputnums);
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