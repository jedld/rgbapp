package com.rgb.matrix;

import java.util.Vector;

/**
 * Created by joseph on 5/8/14.
 */
public class SavedGridBundle {
    private GridSquare[][] world;
    private Vector<NextObject> queue;

    public void setWorld(GridSquare[][] world) {
        this.world = world;
    }

    public GridSquare[][] getWorld() {
        return world;
    }

    public void setQueue(Vector<NextObject> queue) {
        this.queue = queue;
    }

    public Vector<NextObject> getQueue() {
        return queue;
    }
}
