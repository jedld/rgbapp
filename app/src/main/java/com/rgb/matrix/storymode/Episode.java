package com.rgb.matrix.storymode;

import java.util.ArrayList;

/**
 * Created by joseph on 6/26/14.
 */
public class Episode {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<LevelInfo> getLevels() {
        return levels;
    }

    public void setLevels(ArrayList<LevelInfo> levels) {
        this.levels = levels;
    }

    String name;
    ArrayList<LevelInfo> levels;
}
