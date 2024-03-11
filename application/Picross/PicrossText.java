package Picross;

import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.text.Text;

abstract class PicrossText extends Text {
    protected String separator = "";
    protected String suffix = "";
    private int[] nums;
    
    public PicrossText() {}
    
    public void updateText(int[] nums) {
        this.nums = nums;
        setText(Arrays.stream(nums)
                      .mapToObj(String::valueOf)
                      .collect(Collectors.joining(separator)) 
                      + suffix);
    }
    
    public int[] getNums() {
        return nums;
    }

    public void setFocused() {
        getParent().getStyleClass().add("focused");
    }
    
    public void removeFocused() {
        getParent().getStyleClass().remove("focused");
    }
}
