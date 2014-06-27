package com.rgb.matrix;

import com.rgb.matrix.interfaces.BoundedEntity;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import java.util.HashMap;

/**
 * Created by joseph on 6/27/14.
 */
public class TileQueue extends BoundedEntity {

    private final int queueSize;
    private final VertexBufferObjectManager vertexBufferObjectManager;

    public GridSquare[] getQueueRectangles() {
        return queueRectangles;
    }

    public void setQueueRectangles(GridSquare[] queueRectangles) {
        this.queueRectangles = queueRectangles;
    }

    GridSquare queueRectangles[];
    float maxWidth = 0, maxHeight = 0;

    public TileQueue(float pX, float pY, MainGrid grid, int queueSize, float tileSize, HashMap<String, Font> mFont,VertexBufferObjectManager vertexBuffer) {
        super(pX, pY);
        this.queueSize = queueSize;
        this.vertexBufferObjectManager = vertexBuffer;
        this.queueRectangles = new GridSquare[queueSize];
        this.maxHeight = tileSize + 2;
        for (int i = queueSize - 1; i >= 0; i--) {

            float rect_x = (i * (tileSize + 5) + 15);

            GridSquare gridSquare = null;
            if (i == 0) {
                Rectangle container = new Rectangle(rect_x, 0, tileSize + 2, tileSize + 2, vertexBuffer);
                Rectangle border = new Rectangle(1, 1, tileSize, tileSize, vertexBuffer);
                border.setColor(Color.WHITE);
                container.attachChild(border);
                container.setColor(Color.BLACK);
                container.setAlpha(0.2f);
                gridSquare = new GridSquare(-1, -1, 1, 1, grid, mFont, vertexBuffer);
                container.setScaleCenter((tileSize + 2) / 2, (tileSize + 2) / 2);
                container.setScale(1.5f);
                border.attachChild(gridSquare);
                attachChild(container);
            } else {
                gridSquare = new GridSquare(-1, -1, rect_x, 0, grid, mFont, vertexBuffer);
                gridSquare.setScale(1.1f);
                gridSquare.setScaleCenter(tileSize / 2, tileSize / 2);
                attachChild(gridSquare);

            }

            if (maxWidth < rect_x + tileSize + 2) {
                maxWidth = rect_x + tileSize + 2;
            }
;            queueRectangles[i] = gridSquare;
        }


    }

    @Override
    public float getWidth() {
        return maxWidth;
    }

    @Override
    public float getHeight() {
        return maxHeight;
    }
}
