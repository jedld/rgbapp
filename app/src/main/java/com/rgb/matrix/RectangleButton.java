package com.rgb.matrix;


import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

public class RectangleButton extends Entity {

    Rectangle rectangle;

    public RectangleButton(int pX, int pY, int width, int height, VertexBufferObjectManager pVertexBufferObjectManager, Font pFont, String pText) {
        super(pX, pY);
        Text buttonText = new Text(0, 0, pFont, pText, pVertexBufferObjectManager);
        buttonText.setPosition((width - buttonText.getWidth()) / 2, (height - buttonText.getHeight()) / 2);
        buttonText.setColor(Color.WHITE);

        rectangle = new Rectangle(0, 0, width, height, pVertexBufferObjectManager);
        rectangle.setColor(Color.BLACK);

        this.attachChild(rectangle);
        this.attachChild(buttonText);
    }

    public boolean isAreaTouched(TouchEvent pSceneTouchEvent) {
        float[] coordinates = convertLocalToSceneCoordinates(rectangle.getX(), rectangle.getY());
        if (  coordinates[Constants.VERTEX_INDEX_X] <= pSceneTouchEvent.getX() &&  (coordinates[Constants.VERTEX_INDEX_X] + rectangle.getWidth()) >= pSceneTouchEvent.getX() &&
                coordinates[Constants.VERTEX_INDEX_Y] <= pSceneTouchEvent.getY() && (pSceneTouchEvent.getY() <= coordinates[Constants.VERTEX_INDEX_Y] + rectangle.getHeight()) ) {
            return true;
        }
        return false;
    }

}
