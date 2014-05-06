package com.rgb.matrix.interfaces;

import org.andengine.entity.Entity;

/**
 * Created by joseph on 5/6/14.
 */
public abstract class BoundedEntity extends Entity {

    public BoundedEntity(float pX, float pY) {
        super(pX, pY);
    }

    abstract public float getWidth();
    abstract public float getHeight();


}
