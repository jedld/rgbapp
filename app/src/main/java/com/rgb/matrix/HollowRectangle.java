package com.rgb.matrix;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Line;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

/**
 * Created by joseph on 5/3/14.
 */
public class HollowRectangle extends Entity {

    private final Line line1;
    private final Line line2;
    private final Line line3;
    private final Line line4;

    public HollowRectangle(float pX, float pY, float width, float height, Color color, float pLineWidth, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);

        float lineWidthFactor = pLineWidth / 2;
        line1 = new Line(lineWidthFactor, 0, width - lineWidthFactor, 0, vertexBufferObjectManager);
        line1.setLineWidth(pLineWidth);
        line1.setColor(color);

        line2 = new Line(lineWidthFactor, 0,  lineWidthFactor, height , vertexBufferObjectManager);
        line2.setLineWidth(pLineWidth);
        line2.setColor(color);

        line3 = new Line(width - lineWidthFactor, 0, width - lineWidthFactor, height, vertexBufferObjectManager);
        line3.setLineWidth(pLineWidth);
        line3.setColor(color);

        line4 = new Line(lineWidthFactor, height, width - lineWidthFactor, height, vertexBufferObjectManager);
        line4.setLineWidth(pLineWidth);
        line4.setColor(color);

        attachChild(line1);
        attachChild(line2);
        attachChild(line3);
        attachChild(line4);
    }

    public void setColor(Color color) {
        line1.setColor(color);
        line2.setColor(color);
        line3.setColor(color);
        line4.setColor(color);
    }


}
