package Solver;

public class SolutionDisplayStep {
    public final int row;
    public final int col;
    public final int newState;
    public final boolean focusColNums;

    public SolutionDisplayStep(int row, int col, int newState, boolean focusColNums) {
        this.row = row;
        this.col = col;
        this.newState = newState;
        this.focusColNums = focusColNums;
    }
}
