package com.rgb.matrix.storymode;

import java.util.HashMap;

/**
 * Created by joseph on 5/8/14.
 */
public class Levels {
    String firstLevel;

    public HashMap<String, Level> getLevels() {
        return levels;
    }

    public void setLevels(HashMap<String, Level> levels) {
        this.levels = levels;
    }

    public String getFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(String firstLevel) {
        this.firstLevel = firstLevel;
    }

    HashMap<String, Level> levels;
}
