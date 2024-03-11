package Picross;

import javafx.scene.layout.VBox;

class ColumnText extends PicrossText {
    public ColumnText() {
        separator = "\n";
        updateText(new int[] {0});
    }

    public void setHeight(double newHeight) {
        ((VBox) getParent()).setMinHeight(newHeight);
        ((VBox) getParent()).setMaxHeight(newHeight);
    } 
}