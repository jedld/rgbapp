package com.rgb.matrix.menu;

import android.util.Log;

import com.rgb.matrix.Utils;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import java.util.HashMap;

public class MainMenu extends MenuEntity {

    public static final float MENU_WIDTH = 400;
    public static final float MENU_HEIGHT = 600;
    public static final float MENU_ITEM_HEIGHT = 54;
    public static final float MENU_MARGINS = 15;
    private static final float SCENE_WIDTH = 480;
    private static final float SCENE_HEIGHT = 800;
    private static final String TAG = MainMenu.class.getName();

    private final Text backButton;
    private final Rectangle overlayRectangle;

    private OnBackListener onBackListener;


    public MainMenu(float pX, float pY, HashMap<String, Font> fontHashMap, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY, vertexBufferObjectManager);

        overlayRectangle = new Rectangle(0, 0, SCENE_WIDTH, SCENE_HEIGHT, vertexBufferObjectManager);
        overlayRectangle.setColor(Color.BLACK);
        overlayRectangle.setAlpha(0.3f);
        attachChild(overlayRectangle);

        setBackgroundRectangle(new Rectangle( (SCENE_WIDTH / 2) - (MENU_WIDTH / 2),
                (SCENE_HEIGHT / 2) - (MENU_HEIGHT / 2), MENU_WIDTH, MENU_HEIGHT, vertexBufferObjectManager));
        getBackgroundRectangle().setColor(Color.BLACK);
        attachChild(getBackgroundRectangle());

        backButton = new Text(MENU_MARGINS, MENU_MARGINS, fontHashMap.get("menu"), "Back", vertexBufferObjectManager);
        backButton.setColor(Color.WHITE);
        getBackgroundRectangle().attachChild(backButton);

        menuStartOffsetY = 100;
    }

    public void animateShow() {
        setVisible(true);
        overlayRectangle.registerEntityModifier(new AlphaModifier(0.2f, 0f, 0.3f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                getBackgroundRectangle().setAlpha(0f);
                resetChildAlpha();
            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                getBackgroundRectangle().registerEntityModifier(new AlphaModifier(0.2f, 0, 1f, new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        setChildAlpha();
                    }
                }));

            }
        }));

        Utils.getInstance().getSound("menu").play();
    }

    private void setChildAlpha() {
        for(int i = 0; i < getBackgroundRectangle().getChildCount(); i++) {
            IEntity child = getBackgroundRectangle().getChildByIndex(i);
            child.setVisible(true);
            child.registerEntityModifier(new AlphaModifier(0.5f, 0, 1f));
        }
    }

    private void resetChildAlpha() {
        for(int i = 0; i < getBackgroundRectangle().getChildCount(); i++) {
            IEntity child = getBackgroundRectangle().getChildByIndex(i);
            child.setVisible(false);
        }
    }

    public void animateHide() {
        getBackgroundRectangle().registerEntityModifier(new AlphaModifier(0.5f, 1, 0f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                overlayRectangle.registerEntityModifier(new AlphaModifier(0.5f, 0.3f, 0f, new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        setVisible(false);
                    }
                }));
            }
        }));
    }




    public void setOnBackListener(OnBackListener listener) {
        this.onBackListener = listener;
    }

    @Override
    public float getMenuWidth() {
        return MainMenu.MENU_WIDTH;
    }

    public void handleOnTouch(TouchEvent pSceneTouchEvent) {
        Log.d(TAG, "handle onTouch " + pSceneTouchEvent.getX() + "," + pSceneTouchEvent.getY());
        float[] backButtonCoordinates = backButton.getParent().convertLocalToSceneCoordinates(backButton.getX(), backButton.getY());

        if (pSceneTouchEvent.getX() >= backButtonCoordinates[Constants.VERTEX_INDEX_X] &&
                pSceneTouchEvent.getX() <= backButtonCoordinates[Constants.VERTEX_INDEX_X] + backButton.getWidth()
                && pSceneTouchEvent.getY() >= backButtonCoordinates[Constants.VERTEX_INDEX_Y] &&
                pSceneTouchEvent.getY() <= backButtonCoordinates[Constants.VERTEX_INDEX_Y] + backButton.getHeight()) {
            if (onBackListener != null) {
                onBackListener.onBackPressed(this);
            }
        }
        super.handleOnTouch(pSceneTouchEvent);
    }


}
