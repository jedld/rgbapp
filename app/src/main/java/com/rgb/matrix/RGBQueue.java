package com.rgb.matrix;

import org.andengine.entity.Entity;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by joseph on 4/24/14.
 */
public class RGBQueue extends Entity{

    private final VertexBufferObjectManager vertexBufferObjectManager;

    public RGBQueue(float pX, float pY, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        this.vertexBufferObjectManager = vertexBufferObjectManager;
    }
}
