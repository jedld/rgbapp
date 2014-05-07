package com.rgb.matrix.interfaces;

import org.andengine.entity.Entity;

/**
 * Created by joseph on 5/6/14.
 */
public abstract class BoundedEntity extends Entity {

    public static int CENTER_HORIZONTAL = 0x1;
    public static int CENTER_VERTICAL = 0x2;

    public BoundedEntity(float pX, float pY) {
        super(pX, pY);
    }

    abstract public float getWidth();
    abstract public float getHeight();

    public void centerInParent(int flags) {
        if (getParent() instanceof BoundedEntity) {
            BoundedEntity parent = (BoundedEntity)getParent();
            if ( (flags & CENTER_HORIZONTAL) > 0) {
                setX((parent.getWidth() - getWidth()) / 2);
            }
            if ( (flags & CENTER_VERTICAL) > 0) {
                setY( (parent.getHeight() - getHeight()) / 2);
            }
        }
    }

}
