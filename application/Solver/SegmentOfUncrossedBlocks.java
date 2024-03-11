package Solver;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

class SegmentOfUncrossedBlocks {
    private int[] segment;
    protected final int len;
    protected final int linePosition;
    private int[] nums;
    private int[] positions;
    private int numsLen;
    private PicrossSquare[] squares;
    private boolean hasSomeFilled;

    protected SegmentOfUncrossedBlocks(int[] segment, int linePosition) {
        this.segment = segment;
        len = segment.length;
        this.linePosition = linePosition;
        squares = new PicrossSquare[len];

        IntStream.range(0, len)
                 .iterator()
                 .forEachRemaining((IntConsumer) this::createPicrossSquare);
    }

    private void createPicrossSquare(int index) {
        squares[index] = new PicrossSquare();
        if (segment[index] == PicrossSolver.FILLED_CELL) {
            squares[index].setAlreadyFilled();
            hasSomeFilled = true;
        }
    }

    protected boolean notPossibleToFit(int[] theNums) {
        PicrossSolver.debug("Try to fit " + Arrays.toString(theNums) + " in " + Arrays.toString(segment));
        try {
            nums = theNums;
            numsLen = nums.length;

            Optional<Boolean> specialCaseResult = checkNumsSpecialCases();
            if (specialCaseResult.isPresent()) {
                return specialCaseResult.get();
            }

            setInitialPositions();

            try {
                setPositionsToMinimumAcceptable(segment, nums, positions);
                return false;
            } catch (IllegalArgumentException e) {
                PicrossSolver.debug("Nums cannot cover everything");
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            PicrossSolver.debug("Went over the end of the line, couldn't fit them: " + e.getMessage());
            return true;
        }
    }

    // The next two functions are abstracted out in this way partially to ensure positions is not initialized if there is a special case
    private Optional<Boolean> checkNumsSpecialCases() {
        if (numsLen == 0 || nums[0] == 0) {
            PicrossSolver.debug("Are there already filled in spaces? " + hasSomeFilled);
            return Optional.of(hasSomeFilled);
        }

        if (nums[0] == len) { 
            if (numsLen == 1) {
                PicrossSolver.debug("Fits exactly");
                positions = new int[] {0};
                return Optional.of(false);
            } else {
                PicrossSolver.debug("Obviously too large");
                return Optional.of(true);
            }
        }

        return Optional.empty();
    }

    private void setInitialPositions() {
        positions = new int[numsLen];
        int numIndex = 1;
        while (numIndex < numsLen) {
            positions[numIndex] = positions[numIndex - 1] + nums[numIndex - 1] + 1;
            numIndex++;
        }
    }

    private void setPositionsToMinimumAcceptable(int[] ourSegment, int[] ourNums, int[] ourPositions) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        boolean restart = true;
        int index = len - 1;

        PicrossSolver.debug("Moving each over just enough");
        while (restart) {
            restart = false;
            index = len - 1;

            int numIndex = numsLen - 1;
            while (!restart && numIndex >= 0) {
                while (!restart && index >= ourPositions[numIndex] + ourNums[numIndex]) {
                    if (ourSegment[index] == PicrossSolver.FILLED_CELL) {
                        PicrossSolver.debug("Jump num at numIndex " + numIndex + " to index " + (index - ourNums[numIndex] + 1) + " of " + Arrays.toString(ourSegment));
                        ourPositions[numIndex] = index - ourNums[numIndex] + 1;
                        restart = true;
                    }
                    index--;
                }
                preventOverlap(numIndex, ourNums, ourPositions);
                index = ourPositions[numIndex] - 1;
                numIndex--;
            }
        }

        while (index >= 0) {
            if (ourSegment[index] == PicrossSolver.FILLED_CELL) {
                throw new IllegalArgumentException();
            }
            index--;
        }

        PicrossSolver.debug("Found positions " + Arrays.toString(ourPositions));
    }

    private void preventOverlap(int numIndex, int[] ourNums, int[] ourPositions) throws ArrayIndexOutOfBoundsException {
        while (numIndex < numsLen - 1 && ourPositions[numIndex] + ourNums[numIndex] >= ourPositions[numIndex + 1]) {
            ourPositions[numIndex + 1] = ourPositions[numIndex] + ourNums[numIndex] + 1;
            numIndex++;
        }
        if (ourPositions[numIndex] + ourNums[numIndex] > len) {
            throw new ArrayIndexOutOfBoundsException("Went over edge while trying to prevent overlap: index" + numIndex + " of " + Arrays.toString(ourNums) + " at " + Arrays.toString(ourPositions));
        }
    }

    // intended to be called after checking on a possible set of nums, will throw exception otherwise
    protected void assessPossibilities() {
        if (nums == null) {
            throw new RuntimeException("Do not assess possibilitites when no possiblities have been given");
        }

        if (positions == null) {
            if (nums[0] == len) {
                for (PicrossSquare square : squares) {
                    square.setCouldBeFilled();
                }
            } else {
                for (PicrossSquare square : squares) {
                    square.setCouldBeCrossed();
                }
            }
        } else {
            getPossibilitiesFromCurrentPositions();
            moveAllToMaxAndCollectInfo();
        }
    }

    private void getPossibilitiesFromCurrentPositions() throws RuntimeException {
        PicrossSolver.debug("Getting initial new possiblities for segment " + Arrays.toString(segment) + " and nums " + Arrays.toString(nums) + " with positions " + Arrays.toString(positions));
        int index = 0;
        int numIndex = 0;
        while (index < positions[numsLen - 1] + nums[numsLen - 1]) {
            while (index < positions[numIndex]) {
                squares[index].setCouldBeCrossed();
                index++;
            }
            while (index < positions[numIndex] + nums[numIndex]) {
                squares[index].setCouldBeFilled();
                index++;
            }
            numIndex++;
        }
        while (index < len) {
            squares[index].setCouldBeCrossed();
            index++;
        }
    }

    private void moveAllToMaxAndCollectInfo() {
        int[] maxPositions = findMaximumAcceptablePositions();
        int newPosition = positions[numsLen - 1];
        while (newPosition <= maxPositions[numsLen - 1]) {
            moveToDifferentPosition(numsLen - 1, newPosition, positions.clone(), maxPositions);
            newPosition++;
        }
    }

    private int[] findMaximumAcceptablePositions() {
        PicrossSolver.debug("Finding max positions");
        int[] flippedSegment = flippedCopy(segment, len);
        int[] flippedNums = flippedCopy(nums, numsLen);
        int[] flippedPositions = new int[numsLen];
        
        int numIndex = 1;
        while (numIndex < numsLen) {
            flippedPositions[numIndex] = flippedPositions[numIndex - 1] + flippedNums[numIndex - 1] + 1;
            numIndex++;
        }

        setPositionsToMinimumAcceptable(flippedSegment, flippedNums, flippedPositions);

        int[] maxPositions = new int[numsLen];
        numIndex = 0;
        while (numIndex < numsLen) {
            maxPositions[numIndex] = len - flippedPositions[numsLen - 1 - numIndex] - nums[numIndex];
            numIndex++;
        }

        PicrossSolver.debug("Found max " + Arrays.toString(maxPositions));
        return maxPositions;
    }

    private int[] flippedCopy(int[] myArray, int len) {
        int[] flipped = new int[len];
        int index = 0;
        while (index < len) {
            flipped[index] = myArray[len - index - 1];
            index++;
        }
        return flipped;
    }

    private void moveToDifferentPosition(int numIndex, int position, int[] tryingPositions, int[] maxPositions) {
        tryingPositions[numIndex] = position;
        if (numIndex == 0) {
            tryPositions(tryingPositions);
            return;
        }

        int newPosition = positions[numIndex - 1];
        while (newPosition <= Math.min(maxPositions[numIndex - 1], tryingPositions[numIndex] - 1 - nums[numIndex - 1])) {
            moveToDifferentPosition(numIndex - 1, newPosition, tryingPositions, maxPositions);
            newPosition++;
        }
    }

    private void tryPositions(int[] tryingPositions) {
        PicrossSolver.debug("Trying " + Arrays.toString(tryingPositions));
        int index = positions[0];
        int numIndex = 0;
        while (index < tryingPositions[numsLen - 1]) {
            while (index < tryingPositions[numIndex]) {
                if (segment[index] == PicrossSolver.FILLED_CELL) {
                    return;
                }
                index++;
            }
            index = tryingPositions[numIndex] + nums[numIndex];
            numIndex++;
        }

        index = positions[0];
        numIndex = 0;
        while (index < tryingPositions[numsLen - 1] + nums[numsLen - 1]) {
            while (index < tryingPositions[numIndex]) {
                squares[index].setCouldBeCrossed();
                index++;
            }
            while (index < tryingPositions[numIndex] + nums[numIndex]) {
                squares[index].setCouldBeFilled();
                index++;
            }
            numIndex++;
        }
    }

    // Intended to be called only after all segments in the line have been verified to fit assigned nums according to current distribution
    protected int[] getNewInfo() throws RuntimeException {
        int[] newInfo = new int[len];
        IntStream.range(0, len)
                 .iterator()
                 .forEachRemaining((IntConsumer) index -> newInfo[index] = squares[index].concludeDataFromPossibilities());
        
        PicrossSolver.debug("Collected info " + Arrays.toString(newInfo));
        return newInfo;
    }
}