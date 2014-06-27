package com.rgb.matrix.storymode;

/**
 * Created by joseph on 5/8/14.
 */
public class LevelInfo {
    private String filePath;
    private String title;
    private String nextLevel;
    private boolean locked;
    private int id;
    private boolean isComingSoon;

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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setIsComingSoon(boolean isComingSoon) {
        this.isComingSoon = isComingSoon;
    }

    public boolean isComingSoon() {
        return isComingSoon;
    }

    public void setComingSoon(boolean isComingSoon) {
        this.isComingSoon = isComingSoon;
    }
}
