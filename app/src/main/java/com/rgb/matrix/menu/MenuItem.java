package com.rgb.matrix.menu;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;

/**
 * Created by joseph on 5/2/14.
 */

public class MenuItem {

    private String label;
    private OnMenuSelectedListener listener;
    private Rectangle rectangle;
    private Text text;

    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    private boolean stateful;

    public boolean isState() {
        return state;
    }

    boolean state = false;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setListener(OnMenuSelectedListener listener) {
        this.listener = listener;
    }

    public OnMenuSelectedListener getListener() {
        return listener;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Text getText() {
        return text;
    }

    public void setState(boolean state) {
        this.state = state;
        if (state) {
            text.setText(getLabel() + " - ON ");
        } else {
            text.setText(getLabel() + " - OFF ");
        }
    }

    public boolean getState() {
        return state;
    }
}
