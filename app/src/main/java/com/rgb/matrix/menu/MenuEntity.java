package com.rgb.matrix.menu;

import android.util.Log;

import com.rgb.matrix.Utils;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.color.Color;

import java.util.ArrayList;

/**
 * Created by joseph on 5/5/14.
 */
public abstract class MenuEntity extends Entity {
    private static final String TAG = MenuEntity.class.getName();
    protected final VertexBufferObjectManager vertexBufferObjectManager;
    protected ArrayList<MenuItem> items = new ArrayList<MenuItem>();
    private Entity backgroundRectangle;
    private float menuOffsetY;
    protected float menuStartOffsetY;

    public MenuEntity(float pX, float pY, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.menuStartOffsetY = 0;
    }

    public abstract float getMenuWidth();

    public String getFontKey() {
        return "menu";
    }

    public void addMenuItem(String label, OnMenuSelectedListener listener) {
        MenuAttributes attributes = new MenuAttributes();
        attributes.setColor(Color.BLACK);
        attributes.setBackgroundColor(Color.WHITE);
        attributes.setAlpha(1f);
        addMenuItem(label, false, false, attributes, listener);
    }

    public void addMenuItem(String label, boolean isStateful, boolean defaultValue, OnMenuSelectedListener listener) {
        MenuAttributes attributes = new MenuAttributes();
        attributes.setColor(Color.BLACK);
        attributes.setBackgroundColor(Color.WHITE);
        attributes.setAlpha(1f);
        addMenuItem(label, isStateful, defaultValue,
                attributes, listener);
    }

    public void addMenuItem(String label, boolean isStateful, boolean defaultValue,
                            MenuAttributes menuAttributes, OnMenuSelectedListener listener) {
        MenuItem menuItem = new MenuItem();
        menuItem.setLabel(label);
        menuItem.setListener(listener);

        //get next menu item position
        menuOffsetY += MainMenu.MENU_MARGINS + MainMenu.MENU_ITEM_HEIGHT;

        float boundRectangleWidth = getMenuWidth() - MainMenu.MENU_MARGINS * 2;
        Rectangle rectangle = new Rectangle(MainMenu.MENU_MARGINS, menuOffsetY + menuStartOffsetY, boundRectangleWidth,
                MainMenu.MENU_ITEM_HEIGHT, vertexBufferObjectManager);
        rectangle.setColor(menuAttributes.getBackgroundColor());
        rectangle.setAlpha(menuAttributes.getAlpha());
        Text menuText = null;
        if (isStateful) {
            String menuLabel = "";
            if (defaultValue) {
                menuLabel = label + " - ON ";
            } else {
                menuLabel = label + " - OFF";
            }

            menuText = new Text(0, 0, Utils.getInstance().getFont(getFontKey()), menuLabel, vertexBufferObjectManager);
            menuItem.setText(menuText);
            menuItem.setState(defaultValue);
        } else {
            menuText = new Text(0, 0, Utils.getInstance().getFont(getFontKey()), label, vertexBufferObjectManager);
            menuItem.setText(menuText);
        }
        menuText.setX(boundRectangleWidth / 2 - menuText.getWidth() / 2);
        menuText.setY(MainMenu.MENU_ITEM_HEIGHT / 2 - menuText.getHeight() / 2);

        menuText.setColor(menuAttributes.getColor());
        rectangle.attachChild(menuText);
        menuItem.setRectangle(rectangle);
        getBackgroundRectangle().attachChild(rectangle);
        items.add(menuItem);
    }

    public void addGap(float gap) {
        menuOffsetY += gap;
    }

    public void handleOnTouch(TouchEvent pSceneTouchEvent) {
        ArrayList<MenuItem> triggerEvents = new ArrayList<MenuItem>();
        for (MenuItem item : items) {

            float[] coordinates = getBackgroundRectangle().convertLocalToSceneCoordinates(item.getRectangle().getX(), item.getRectangle().getY());
            float rectWidth = item.getRectangle().getWidth();
            float rectHeight = item.getRectangle().getHeight();

            Log.d(TAG, "menu coords " + coordinates[Constants.VERTEX_INDEX_X] + " , " + coordinates[Constants.VERTEX_INDEX_Y]);

            if (pSceneTouchEvent.getX() >= coordinates[Constants.VERTEX_INDEX_X] &&
                    pSceneTouchEvent.getX() <= coordinates[Constants.VERTEX_INDEX_X] + rectWidth
                    && pSceneTouchEvent.getY() >= coordinates[Constants.VERTEX_INDEX_Y] &&
                    pSceneTouchEvent.getY() <= coordinates[Constants.VERTEX_INDEX_Y] + rectHeight) {

                triggerEvents.add(item);
            }
        }

        for(MenuItem item : triggerEvents) {
            item.getListener().onMenuItemSelected(item);
        }

        triggerEvents.clear();

    }

    public Entity getBackgroundRectangle() {
        return backgroundRectangle;
    }

    public void setBackgroundRectangle(Rectangle backgroundRectangle) {
        this.backgroundRectangle = backgroundRectangle;
    }

    public void clearItems() {
        items.clear();
        menuOffsetY = 0;
    }
}
