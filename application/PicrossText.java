package application;

import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.text.Text;

public class PicrossText extends Text {
	private String separator;
	private int[] nums;
	
	public PicrossText(int[] nums, String separator) {
		this.separator = separator;
		updateText(nums);
	}
	
	public void updateText(int[] blocks) {
		this.nums = blocks;
		String newText = Arrays.stream(blocks)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining(separator));
		if (separator == " ") {
			setText(newText + "\u2009");
		} else {
			setText(newText);
		}
	}
	
	public int[] getNums() {
		return nums;
	}
}