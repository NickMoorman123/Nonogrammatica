package Solver;

class PicrossSquare {
    private static enum Possibility { YES, NO, UNKNOWN }
    private Possibility crossed = Possibility.UNKNOWN;
    private Possibility filled = Possibility.UNKNOWN;

    protected void setCouldBeCrossed() throws RuntimeException {
        if (crossed == Possibility.NO) {
            throw new RuntimeException("Tried to overwrite square");
        }
        crossed = Possibility.YES;
    }

    protected void setCouldBeFilled() {
        filled = Possibility.YES;
    }

    protected void setAlreadyFilled() {
        filled = Possibility.YES;
        crossed = Possibility.NO;
    }

    protected int concludeDataFromPossibilities() {
        if (canConcludeFilled()) {
            return PicrossSolver.FILLED_CELL;
        } else if (canConcludeCrossed()) {
            return PicrossSolver.CROSSED_CELL; 
        } else {
            return PicrossSolver.EMPTY_CELL;
        }
    }

    private boolean canConcludeCrossed() {
        return crossed == Possibility.YES && filled != Possibility.YES;
    }

    private boolean canConcludeFilled() {
        return filled == Possibility.YES && crossed != Possibility.YES;
    }
}
