package com.rgb.matrix;


import com.rgb.matrix.interfaces.BoundedEntity;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.IFont;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

public class RectangleButton extends BoundedEntity {

    public static final int SPRITE_RIGHT_MARGIN = 10;
    private static final float SPRITE_LEFT_MARGIN = 10;
    private final Text buttonText;
    private float height;

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    private float width;
    Rectangle rectangle;

    public RectangleButton(float pX, float pY, float width, float height,
                           VertexBufferObjectManager pVertexBufferObjectManager,
                           IFont pFont, String pText) {
        super(pX, pY);
        this.height = height;
        this.width = width;
        buttonText = new Text(0, 0, pFont, pText, pVertexBufferObjectManager);
        rectangle = new Rectangle(0, 0, width, height, pVertexBufferObjectManager);
        rectangle.setColor(Color.BLACK);
        reposition();
        this.attachChild(rectangle);
        this.attachChild(buttonText);
    }

    public void setTextColor(Color color) {
        buttonText.setColor(color);
    }

    @Override
    public void setColor(Color color) {
        rectangle.setColor(color);
    }

    public void setSprite(Sprite sprite) {
        sprite.setSize(buttonText.getHeight(), buttonText.getHeight());
        buttonText.setX(buttonText.getX() + sprite.getWidth());
        sprite.setX(SPRITE_LEFT_MARGIN);
        sprite.setY((height - sprite.getHeight()) / 2);

        attachChild(sprite);
    }

    @Override
    public void setAlpha(float alpha) {
        buttonText.setAlpha(alpha);
        rectangle.setAlpha(alpha);
    }

    public boolean isAreaTouched(TouchEvent pSceneTouchEvent) {
        float[] coordinates = convertLocalToSceneCoordinates(rectangle.getX(), rectangle.getY());
        if (  coordinates[Constants.VERTEX_INDEX_X] <= pSceneTouchEvent.getX() &&  (coordinates[Constants.VERTEX_INDEX_X] + rectangle.getWidth()) >= pSceneTouchEvent.getX() &&
                coordinates[Constants.VERTEX_INDEX_Y] <= pSceneTouchEvent.getY() && (pSceneTouchEvent.getY() <= coordinates[Constants.VERTEX_INDEX_Y] + rectangle.getHeight()) ) {
            return true;
        }
        return false;
    }

    public void autoWidth(float margin) {
        width = buttonText.getWidth() + margin * 2;
        height = buttonText.getHeight() + margin * 2;
        reposition();
    }

    private void reposition() {
        buttonText.setPosition((width - buttonText.getWidth()) / 2, (height - buttonText.getHeight()) / 2);
        buttonText.setColor(Color.WHITE);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
    }
}
