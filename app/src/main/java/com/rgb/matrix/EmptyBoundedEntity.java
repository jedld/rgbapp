package com.rgb.matrix;

import com.rgb.matrix.interfaces.BoundedEntity;

/**
 * Created by joseph on 5/7/14.
 */
public class EmptyBoundedEntity extends BoundedEntity {

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    private float width;
    private float height;

    public EmptyBoundedEntity(float pX, float pY, float width, float height) {
        super(pX, pY);
        this.width = width;
        this.height = height;
    }
    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }


}
