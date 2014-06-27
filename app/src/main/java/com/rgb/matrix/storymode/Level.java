package com.rgb.matrix.storymode;

import com.rgb.matrix.NextObject;

import java.util.ArrayList;

/**
 * Created by joseph on 5/5/14.
 */
public class Level {

    int nextLevel;
    int gridHeight, gridWidth;
    String initialPositions;
    String name;
    private boolean rechargeMeter;
    private boolean useQueue;
    private CharSequence subName;
    private boolean scoreVisible;
    private int rechargeMeterInitial;

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    ArrayList<Operation> operations = new ArrayList<Operation>();

    public int[] getQueue() {
        return queue;
    }

    public void setQueue(int[] queue) {
        this.queue = queue;
    }

    int queue[];
    int id;

    public NextObject[][] getMap() {
        return map;
    }

    public void setMap(NextObject[][] map) {
        this.map = map;
    }

    NextObject map[][];

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


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(int nextLevel) {
        this.nextLevel = nextLevel;
    }


    public void setRechargeMeter(boolean rechargeMeter) {
        this.rechargeMeter = rechargeMeter;
    }

    public boolean isRechargeMeter() {
        return rechargeMeter;
    }

    public void setUseQueue(boolean useQueue) {
        this.useQueue = useQueue;
    }

    public boolean isUseQueue() {
        return useQueue;
    }

    public CharSequence getSubName() {
        return subName;
    }

    public void setSubName(CharSequence subName) {
        this.subName = subName;
    }

    public boolean isScoreVisible() {
        return scoreVisible;
    }

    public void setScoreVisible(boolean scoreVisible) {
        this.scoreVisible = scoreVisible;
    }

    public void setRechargeMeterInitial(int rechargeMeterInitial) {
        this.rechargeMeterInitial = rechargeMeterInitial;
    }

    public int getRechargeMeterInitial() {
        return rechargeMeterInitial;
    }
}
