package com.rgb.matrix.menu;

import android.util.Log;

import com.rgb.matrix.MainGrid;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.color.Color;

import java.util.ArrayList;
import java.util.HashMap;

public class MainMenu extends Entity {

    public static final float MENU_WIDTH = 400;
    public static final float MENU_HEIGHT = 600;
    public static final float MENU_ITEM_HEIGHT = 54;
    public static final float MENU_MARGINS = 15;
    private static final float SCENE_WIDTH = 480;
    private static final float SCENE_HEIGHT = 800;
    private static final String TAG = MainMenu.class.getName();

    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final HashMap<String, Font> fontHashMap;
    private final Rectangle backgroundRectangle;
    private final Text backButton;
    private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
    private OnBackListener onBackListener;

    public MainMenu(float pX, float pY, HashMap<String, Font> fontHashMap, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        this.fontHashMap = fontHashMap;
        this.vertexBufferObjectManager = vertexBufferObjectManager;

        Rectangle overlayRectangle = new Rectangle(0, 0, SCENE_WIDTH, SCENE_HEIGHT, vertexBufferObjectManager);
        overlayRectangle.setColor(Color.BLACK);
        overlayRectangle.setAlpha(0.3f);
        attachChild(overlayRectangle);

        backgroundRectangle = new Rectangle( (SCENE_WIDTH / 2) - (MENU_WIDTH / 2),
                (SCENE_HEIGHT / 2) - (MENU_HEIGHT / 2), MENU_WIDTH, MENU_HEIGHT, vertexBufferObjectManager);
        backgroundRectangle.setColor(Color.BLACK);
        attachChild(backgroundRectangle);

        backButton = new Text(MENU_MARGINS, MENU_MARGINS, fontHashMap.get("menu"), "Back", vertexBufferObjectManager);
        backButton.setColor(Color.WHITE);
        backgroundRectangle.attachChild(backButton);
    }

    public void addMenuItem(String label, boolean isStateful, boolean defaultValue, OnMenuSelectedListener listener) {
        MenuItem menuItem = new MenuItem();
        menuItem.setLabel(label);
        menuItem.setListener(listener);

        float boundRectangleWidth = MENU_WIDTH - MENU_MARGINS * 2;
        Rectangle rectangle = new Rectangle(MENU_MARGINS, items.size() * (MENU_MARGINS + MENU_ITEM_HEIGHT) + 100, boundRectangleWidth,
                MENU_ITEM_HEIGHT, vertexBufferObjectManager);
        rectangle.setColor(Color.WHITE);
        Text menuText = null;
        if (isStateful) {
            String menuLabel = "";
            if (defaultValue) {
                menuLabel = label + " - ON ";
            } else {
                menuLabel = label + " - OFF";
            }

            menuText = new Text(0, 0, fontHashMap.get("menu"), menuLabel, vertexBufferObjectManager);
            menuItem.setText(menuText);
            menuItem.setState(defaultValue);
        } else {
           menuText = new Text(0, 0, fontHashMap.get("menu"), label, vertexBufferObjectManager);
            menuItem.setText(menuText);
        }
        menuText.setX(boundRectangleWidth/2 - menuText.getWidth()/2);
        menuText.setY(MENU_ITEM_HEIGHT/2 - menuText.getHeight()/2);
        menuText.setColor(Color.BLACK);
        rectangle.attachChild(menuText);
        menuItem.setRectangle(rectangle);


        backgroundRectangle.attachChild(rectangle);
        items.add(menuItem);
    }

    public void addMenuItem(String label, OnMenuSelectedListener listener) {
        addMenuItem(label, false, false, listener);
    }


    public void setOnBackListener(OnBackListener listener) {
        this.onBackListener = listener;
    }

    public void handleOnTouch(TouchEvent pSceneTouchEvent) {
        Log.d(TAG, "handle onTouch " + pSceneTouchEvent.getX() + "," + pSceneTouchEvent.getY());
        float[] backButtonCoordinates = backButton.getParent().convertLocalToSceneCoordinates(backButton.getX(), backButton.getY());

        if (pSceneTouchEvent.getX() >= backButtonCoordinates[Constants.VERTEX_INDEX_X] &&
                pSceneTouchEvent.getX() <= backButtonCoordinates[Constants.VERTEX_INDEX_X] + backButton.getWidth()
                && pSceneTouchEvent.getY() >= backButtonCoordinates[Constants.VERTEX_INDEX_Y] &&
                pSceneTouchEvent.getY() <= backButtonCoordinates[Constants.VERTEX_INDEX_Y] + backButton.getHeight()) {
            if (onBackListener!=null) {
                onBackListener.onBackPressed(this);
            }


        }


        for (MenuItem item : items) {

            float[] coordinates = backgroundRectangle.convertLocalToSceneCoordinates(item.getRectangle().getX(), item.getRectangle().getY());
            float rectWidth = item.getRectangle().getWidth();
            float rectHeight = item.getRectangle().getHeight();

            Log.d(TAG,"menu coords " + coordinates[Constants.VERTEX_INDEX_X] + " , " + coordinates[Constants.VERTEX_INDEX_Y]);

            if (pSceneTouchEvent.getX() >= coordinates[Constants.VERTEX_INDEX_X] &&
                pSceneTouchEvent.getX() <= coordinates[Constants.VERTEX_INDEX_X] + rectWidth
                && pSceneTouchEvent.getY() >= coordinates[Constants.VERTEX_INDEX_Y] &&
                pSceneTouchEvent.getY() <= coordinates[Constants.VERTEX_INDEX_Y] + rectHeight) {

               item.getListener().onMenuItemSelected(item);

            }
        }

    }
}
