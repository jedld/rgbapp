package com.rgb.matrix;

import com.rgb.matrix.interfaces.BoundedEntity;

/**
 * Created by joseph on 5/7/14.
 */
public class EmptyBoundedEntity extends BoundedEntity {

    private final float width;
    private final float height;

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
