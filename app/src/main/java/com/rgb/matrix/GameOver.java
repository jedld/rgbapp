package com.rgb.matrix;

import com.rgb.matrix.interfaces.GameOverDialogEventListener;
import com.rgb.matrix.interfaces.GridEventListener;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.color.Color;

/**
 * Created by joseph on 5/4/14.
 */
public class GameOver extends Entity {

    public static final String TAG = GameOver.class.getName();

    private static final int GAMEOVER_DIALOG_WIDTH = 400;
    private static final int GAMEOVER_DIALOG_HEIGHT = 500;
    private static final int FB_SHARE_BUTTON_HEIGHT = 54;
    private static final float GAME_OVER_DIALOG_MARGIN = 54;
    private static final float SHARE_BUTTON_MARGIN = 25;
    public static final Color FB_SHARE_BUTTON_COLOR = new Color(0x39 / 255f, 0x50 / 255f, 0x8F / 255f);

    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final Text gameOverText;
    private final RectangleButton shareButton;

    public GameOver(float pX, float pY, Font mFont, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        this.vertexBufferObjectManager = vertexBufferObjectManager;

        Rectangle rectangle = new Rectangle(0, 0, GAMEOVER_DIALOG_WIDTH, GAMEOVER_DIALOG_HEIGHT, vertexBufferObjectManager);
        rectangle.setColor(Color.BLACK);
        rectangle.setAlpha(0.8f);
        gameOverText = new Text(0, GAME_OVER_DIALOG_MARGIN, mFont, "G A M E   O V E R", vertexBufferObjectManager);
        gameOverText.setX(GAMEOVER_DIALOG_WIDTH / 2 - gameOverText.getWidth() / 2);
        gameOverText.setColor(Color.RED);

        shareButton = new RectangleButton(SHARE_BUTTON_MARGIN, GAMEOVER_DIALOG_HEIGHT - SHARE_BUTTON_MARGIN - FB_SHARE_BUTTON_HEIGHT,
                GAMEOVER_DIALOG_WIDTH  - SHARE_BUTTON_MARGIN * 2, FB_SHARE_BUTTON_HEIGHT,
                vertexBufferObjectManager, mFont, "Share on Facebook");
        shareButton.setColor(Color.WHITE);
        shareButton.setTextColor(FB_SHARE_BUTTON_COLOR);
        shareButton.setSprite(Utils.getInstance().getSprite("fb_icon"));
        attachChild(rectangle);
        attachChild(gameOverText);
        attachChild(shareButton);
    }

    public float getWidth() {
        return GAMEOVER_DIALOG_WIDTH;
    }

    public int getHeight() {
        return GAMEOVER_DIALOG_HEIGHT;
    }


    public void handleTouch(TouchEvent pSceneTouchEvent, GameOverDialogEventListener listener) {
        if (Utils.withinTouchBounds(shareButton, pSceneTouchEvent)) {
            listener.onShare(shareButton);
        }
    }
}
