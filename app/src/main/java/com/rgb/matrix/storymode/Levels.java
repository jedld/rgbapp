package com.rgb.matrix.storymode;

import java.util.HashMap;

/**
 * Created by joseph on 5/8/14.
 */
public class Levels {
    String firstLevel;
    private String filePath;
    private String title;
    private String nextLevel;

    public HashMap<String, LevelInfo> getLevels() {
        return levels;
    }

    public void setLevels(HashMap<String, LevelInfo> levels) {
        this.levels = levels;
    }

    public String getFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(String firstLevel) {
        this.firstLevel = firstLevel;
    }

    HashMap<String, LevelInfo> levels;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setNextLevel(String nextLevel) {
        this.nextLevel = nextLevel;
    }

    public String getNextLevel() {
        return nextLevel;
    }
}
