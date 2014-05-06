package com.rgb.matrix.storymode;

/**
 * Created by joseph on 5/5/14.
 */
public class Level {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitialPositions() {
        return initialPositions;
    }

    public void setInitialPositions(String initialPositions) {
        this.initialPositions = initialPositions;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    int id;

    public int getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(int nextLevel) {
        this.nextLevel = nextLevel;
    }

    int nextLevel;
    int gridHeight, gridWidth;
    String initialPositions;



}
