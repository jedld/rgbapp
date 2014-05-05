package com.rgb.matrix.intro;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by joseph on 5/2/14.
 */
public class PositionsInfo {
    ArrayList<TargetPosition> finalPositions;

    public ArrayList<TargetPosition> getFinalPositions() {
        return finalPositions;
    }

    public void setFinalPositions(ArrayList<TargetPosition> finalPositions) {
        this.finalPositions = finalPositions;
    }

    public float getMaxTextWidth() {
        return maxTextWidth;
    }

    public void setMaxTextWidth(float maxTextWidth) {
        this.maxTextWidth = maxTextWidth;
    }

    public float getMaxTextHeight() {
        return maxTextHeight;
    }

    public void setMaxTextHeight(float maxTextHeight) {
        this.maxTextHeight = maxTextHeight;
    }

    public float getTextOffsetX() {
        return textOffsetX;
    }

    public void setTextOffsetX(float textOffsetX) {
        this.textOffsetX = textOffsetX;
    }

    public float getTextOffsetY() {
        return textOffsetY;
    }

    public void setTextOffsetY(float textOffsetY) {
        this.textOffsetY = textOffsetY;
    }

    float maxTextWidth = 0;
    float maxTextHeight = 0;
    float textOffsetX = 0;
    float textOffsetY = 0;
}
