package com.rgb.matrix.storymode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by joseph on 6/27/14.
 */
public class TileSet {
    private ArrayList<Integer> allowedObjects;
    private double weight;

    public void setAllowedObjects(ArrayList<Integer> allowedObjects) {
        this.allowedObjects = allowedObjects;
    }

    public ArrayList<Integer> getAllowedObjects() {
        return allowedObjects;
    }


    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
