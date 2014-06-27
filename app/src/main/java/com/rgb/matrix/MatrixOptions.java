package com.rgb.matrix;

import com.rgb.matrix.storymode.TileSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by joseph on 5/7/14.
 */
public class MatrixOptions {
    boolean shouldPrepopulate;
    private boolean scoreVisible;
    private int rechargeMeterInitialValue;
    private ArrayList<TileSet> allowedObjects;

    public boolean isShouldShowRechargeMeter() {
        return shouldShowRechargeMeter;
    }

    public void setShouldShowRechargeMeter(boolean shouldShowRechargeMeter) {
        this.shouldShowRechargeMeter = shouldShowRechargeMeter;
    }

    boolean shouldShowRechargeMeter;

    public MatrixOptions() {
        this.shouldPrepopulate = true;
        this.shouldUseRandomQueue = true;
        this.shouldShowRechargeMeter = true;
        this.scoreVisible = true;
    }

    public boolean isShouldUseRandomQueue() {
        return shouldUseRandomQueue;
    }

    public void setShouldUseRandomQueue(boolean shouldUseRandomQueue) {
        this.shouldUseRandomQueue = shouldUseRandomQueue;
    }

    public boolean isShouldPrepopulate() {
        return shouldPrepopulate;
    }

    public void setShouldPrepopulate(boolean shouldPrepopulate) {
        this.shouldPrepopulate = shouldPrepopulate;
    }

    boolean shouldUseRandomQueue;

    public boolean isScoreVisible() {
        return scoreVisible;
    }

    public void setScoreVisible(boolean scoreVisible) {
        this.scoreVisible = scoreVisible;
    }

    public void setRechargeMeterInitialValue(int rechargeMeterInitialValue) {
        this.rechargeMeterInitialValue = rechargeMeterInitialValue;
    }

    public int getRechargeMeterInitialValue() {
        return rechargeMeterInitialValue;
    }

    public void setAllowedObjects(ArrayList<TileSet> allowedObjects) {
        this.allowedObjects = allowedObjects;
    }

    public ArrayList<TileSet> getAllowedObjects() {
        return allowedObjects;
    }
}
