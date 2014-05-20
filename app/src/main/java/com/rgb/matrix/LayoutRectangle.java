package com.rgb.matrix;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.primitive.vbo.IRectangleVertexBufferObject;
import org.andengine.opengl.vbo.DrawType;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by joseph on 5/20/14.
 */
public class LayoutRectangle extends Rectangle {
    public LayoutRectangle(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
        setAnchorCenter(0,0);
    }

    public LayoutRectangle(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager, DrawType pDrawType) {
        super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager, pDrawType);
        setAnchorCenter(0,0);
    }

    public LayoutRectangle(float pX, float pY, float pWidth, float pHeight, IRectangleVertexBufferObject pRectangleVertexBufferObject) {
        super(pX, pY, pWidth, pHeight, pRectangleVertexBufferObject);
        setAnchorCenter(0,0);
    }
}
