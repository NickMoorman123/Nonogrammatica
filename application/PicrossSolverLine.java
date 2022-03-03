package application;

import java.util.Arrays;

public class PicrossSolverLine {
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
}