package com.rgb.matrix;


import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

public class RectangleButton extends Entity {

    Rectangle rectangle;

    public RectangleButton(int pX, int pY, int width, int height, VertexBufferObjectManager pVertexBufferObjectManager, Font pFont, String pText) {
        super(pX, pY);
        Text buttonText = new Text(0, 0, pFont, pText, pVertexBufferObjectManager);
        buttonText.setPosition((width - buttonText.getWidth()) / 2, (height - buttonText.getHeight()) / 2);
        buttonText.setColor(Color.BLACK);

        rectangle = new Rectangle(0, 0, width, height, pVertexBufferObjectManager);
        rectangle.setColor(Color.WHITE);

        this.attachChild(rectangle);
        this.attachChild(buttonText);
    }

    public boolean isAreaTouched(TouchEvent pSceneTouchEvent) {

        if ( getX() <= pSceneTouchEvent.getX() &&  (getX() + rectangle.getWidth()) >= pSceneTouchEvent.getX() &&
                 getY() <= pSceneTouchEvent.getY() && (pSceneTouchEvent.getY() <= getY() + rectangle.getHeight()) ) {
            return true;
        }
        return false;
    }

}
