package com.rgb.matrix.menu;


import org.andengine.util.adt.color.Color;

/**
 * Created by joseph on 5/5/14.
 */
public class MenuAttributes {
    Color color;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    float alpha;
    Color backgroundColor;
}
