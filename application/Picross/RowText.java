package Picross;

import javafx.scene.layout.HBox;

class RowText extends PicrossText {
    public RowText() {
        separator = " ";
        suffix = "\u2009";
        updateText(new int[] {0});
    }

    public void setWidth(double newWidth) {
        ((HBox) getParent()).setMinWidth(newWidth);
		((HBox) getParent()).setMaxWidth(newWidth);
    }
}