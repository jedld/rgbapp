package com.rgb.matrix.intro;

import android.util.Pair;

import org.andengine.util.adt.color.Color;


/**
 * Created by joseph on 5/5/14.
 */
public class TargetPosition {
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Pair<Float, Float> getPosition() {
        return position;
    }

    public void setPosition(Pair<Float, Float> position) {
        this.position = position;
    }

    Color color;
    Pair<Float, Float> position;
}
