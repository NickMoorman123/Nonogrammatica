package Solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

abstract class PicrossLineSolver {

    protected static int[] tryToSolveLine(int[] line, int[] nums) throws RuntimeException {
        PicrossSolver.debug("Given line " + Arrays.toString(line) + " and nums " + Arrays.toString(nums));
        int lineLength = line.length;
        int numsCount = nums.length;

        if (nums[0] == 0) {
            PicrossSolver.debug("Line is confimed crossed out");
            int[] newLine = new int[lineLength];
            Arrays.fill(newLine, PicrossSolver.CROSSED_CELL);
            PicrossSolver.debug("Returning line " + Arrays.toString(newLine));
            return newLine;
        }

        SegmentOfUncrossedBlocks[] segments = findSegments(line, lineLength);
        int countSegments = segments.length;
        PicrossSolver.debug("Found " + countSegments + " segments total");
        int[] distribution = new int[numsCount];

        recursivelyTryDistributions(0, 0, 0, segments, countSegments, distribution, nums, numsCount, false);
        
        int[] newLine = line.clone();
        recursivelyWriteSegmentsToNewLine(newLine, segments, 0);
        
        PicrossSolver.debug("Returning line " + Arrays.toString(newLine));
        return newLine;
    }

    private static SegmentOfUncrossedBlocks[] findSegments(int[] line, int lineLength) {
        ArrayList<SegmentOfUncrossedBlocks> segmentsList = new ArrayList<>();

        int startIndex = 0;
        int currentIndex = 0;
        while (currentIndex < lineLength) {
            if (line[currentIndex] == PicrossSolver.CROSSED_CELL) {
                currentIndex++;
                continue;
            }
            
            startIndex = currentIndex;
            while (currentIndex < lineLength && line[currentIndex] != PicrossSolver.CROSSED_CELL) {
                currentIndex++;
            }
            PicrossSolver.debug("Found segment: " + Arrays.toString(Arrays.copyOfRange(line, startIndex, currentIndex)));
            segmentsList.add(new SegmentOfUncrossedBlocks(Arrays.copyOfRange(line, startIndex, currentIndex), startIndex));
        }

        return segmentsList.toArray(new SegmentOfUncrossedBlocks[segmentsList.size()]);
    }

    private static boolean recursivelyTryDistributions(int numIndex, int segmentIndex, int spacesTaken, SegmentOfUncrossedBlocks[] segments, int countSegments, int[] distribution, int[] nums, int numsCount, boolean foundPassing) throws RuntimeException {
        // if the number fits in the room left in the segment, put it there, then either try the distribution or move on to the next number
        if (segments[segmentIndex].len - spacesTaken >= nums[numIndex]) {
            distribution[numIndex] = segmentIndex;
            if (numIndex == numsCount - 1) {
                foundPassing = tryDistribution(distribution, nums, numsCount, segments, countSegments) || foundPassing;
            } else {
                foundPassing = recursivelyTryDistributions(numIndex + 1, segmentIndex, spacesTaken + nums[numIndex] + 1, segments, countSegments, distribution, nums, numsCount, foundPassing) || foundPassing;
            }
        }

        // try the rest of the numbers in the rest of the segments starting with putting the next number in each of the other segments
        for (int beginIndexForRest = segmentIndex + 1; beginIndexForRest < countSegments; beginIndexForRest++) {
            if (segments[beginIndexForRest].len < nums[numIndex]) {
                continue;
            }

            distribution[numIndex] = beginIndexForRest;
            if (numIndex == numsCount - 1) {
                foundPassing = tryDistribution(distribution, nums, numsCount, segments, countSegments) || foundPassing;
            } else {
                foundPassing = recursivelyTryDistributions(numIndex + 1, beginIndexForRest, nums[numIndex] + 1, segments, countSegments, distribution, nums, numsCount, foundPassing) || foundPassing;
            }
        }

        // account for cases where the first n segments are empty
        if (numIndex == 0 && segmentIndex < countSegments - 1) {
            foundPassing = recursivelyTryDistributions(0, segmentIndex + 1, 0, segments, countSegments, distribution, nums, numsCount, foundPassing) || foundPassing;
        }

        if (distribution[0] == countSegments - 1 && !foundPassing) {
            throw new IllegalStateException("No distribution passed");
        } 
        return foundPassing;
    }

    private static boolean tryDistribution(int[] distribution, int[] nums, int numsCount, SegmentOfUncrossedBlocks[] segments, int countSegments) throws RuntimeException {
        PicrossSolver.debug("Trying distribution " + Arrays.toString(distribution));
        int previousSegmentIndex = -1;
        int leftNumsIndex = 0;
        int rightNumsIndex = 1;
        while (rightNumsIndex <= numsCount) {
            int segmentIndex = distribution[leftNumsIndex]; 

            if (segmentsCantBeEmpty(segments, previousSegmentIndex + 1, segmentIndex)) {
                return false;
            }

            while (rightNumsIndex < numsCount && distribution[rightNumsIndex] == segmentIndex) {
                rightNumsIndex++;
            }
            if (segments[segmentIndex].notPossibleToFit(Arrays.copyOfRange(nums, leftNumsIndex, rightNumsIndex))) {
                return false;
            }

            previousSegmentIndex = segmentIndex;
            leftNumsIndex = rightNumsIndex;
            rightNumsIndex++;
        }
        if (segmentsCantBeEmpty(segments, previousSegmentIndex + 1, countSegments)) {
            return false;
        }
        PicrossSolver.debug("Distribution passed: " + Arrays.toString(distribution));

        for (SegmentOfUncrossedBlocks segment : segments) {
            segment.assessPossibilities();
        }
        return true;
    }

    private static boolean segmentsCantBeEmpty(SegmentOfUncrossedBlocks[] segments, int start, int end) {
        PicrossSolver.debug("Check segments " + start + " to " + end + " cannot be empty");
        return IntStream.range(start, end)
                        .anyMatch(index -> segments[index].notPossibleToFit(new int[] {0}));
    }

    private static void recursivelyWriteSegmentsToNewLine(int[] newLine, SegmentOfUncrossedBlocks[] segments, int segmentIndex) {
        SegmentOfUncrossedBlocks segment = segments[segmentIndex];
        int[] newInfo = segment.getNewInfo();
        
        Iterator<Integer> iterator = IntStream.range(0, segment.len).iterator();
        iterator.forEachRemaining(index -> newLine[segment.linePosition + index] = newInfo[index]);

        if (segmentIndex < segments.length - 1) {
            recursivelyWriteSegmentsToNewLine(newLine, segments, segmentIndex + 1);
        }
    }
}
