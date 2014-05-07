package com.rgb.matrix.storymode;

import com.rgb.matrix.SavedGridBundle;

import java.util.LinkedList;

/**
 * Created by joseph on 5/8/14.
 */
public class SavedGameState {
    private SavedGridBundle savedGridBundle;
    private LinkedList<CurrentBlock> stack;

    public void setSavedGridBundle(SavedGridBundle savedGridBundle) {
        this.savedGridBundle = savedGridBundle;
    }

    public SavedGridBundle getSavedGridBundle() {
        return savedGridBundle;
    }

    public void setStack(LinkedList<CurrentBlock> stack) {
        this.stack = stack;
    }

    public LinkedList<CurrentBlock> getStack() {
        return stack;
    }
}
