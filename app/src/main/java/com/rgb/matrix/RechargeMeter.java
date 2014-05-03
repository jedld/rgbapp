package com.rgb.matrix;

import android.util.Log;

import com.dayosoft.tiletron.app.SoundWrapper;
import com.rgb.matrix.interfaces.Resizable;
import com.rgb.matrix.modifiers.WidthModifier;

import org.andengine.entity.Entity;
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
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * Created by joseph on 5/1/14.
 */
public class RechargeMeter extends Entity implements Resizable {

    private static final String TAG = RechargeMeter.class.getName();
    private final VertexBufferObjectManager vertexBufferObjectManager;
    private final Rectangle background;
    private final Rectangle pushButtonBackground;
    private final float width;
    private final Rectangle pushButton;
    private final Text valueText;
    private final HashMap<String, SoundWrapper> soundAssets;
    private int maxunits;
    private final Rectangle meterObject;
    private int currentState;
    private int prevPoints;
    private boolean isSuperActivated;
    private int level;
    private boolean animatePending;

    public RechargeMeter(float pX, float pY, float width, int maxunits, HashMap<String, Font> mFont, HashMap<String, SoundWrapper> soundAssets, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY);

        this.maxunits = maxunits;
        this.animatePending = false;
        this.level = 1;
        this.soundAssets = soundAssets;
        this.isSuperActivated = false;
        this.currentState = 0;
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.width = width;
        background = new Rectangle(0, 0, width , ObjectDimensions.szMeterHeight, vertexBufferObjectManager);
        background.setColor(Color.BLACK);

        pushButtonBackground = new Rectangle(width - ObjectDimensions.szPushButtonSize, 0, ObjectDimensions.szPushButtonSize, ObjectDimensions.szPushButtonSize, vertexBufferObjectManager);
        pushButtonBackground.setColor(Color.BLACK);

        pushButton = new Rectangle(width - ObjectDimensions.szPushButtonSize + ObjectDimensions.szPushButtonMargin, ObjectDimensions.szPushButtonMargin, ObjectDimensions.szPushButtonSize - ObjectDimensions.szPushButtonMargin * 2, ObjectDimensions.szPushButtonSize - ObjectDimensions.szPushButtonMargin * 2, vertexBufferObjectManager);
        pushButton.setColor(Color.WHITE);

        int pushButtonCenterSize = ObjectDimensions.szPushButtonSize - ObjectDimensions.szPushButtonCenterMargin;
        Rectangle pushButtonCenter = new Rectangle( (ObjectDimensions.szPushButtonSize - ObjectDimensions.szPushButtonMargin * 2) / 2 -  pushButtonCenterSize /2,
                (ObjectDimensions.szPushButtonSize - ObjectDimensions.szPushButtonMargin * 2) / 2 -  pushButtonCenterSize /2, pushButtonCenterSize, pushButtonCenterSize, vertexBufferObjectManager);

        final AlphaModifier blinkModifer = new AlphaModifier(1, 0f, 1f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                pModifier.reset();
            }
        });

        pushButtonCenter.registerEntityModifier(blinkModifer);
        pushButtonCenter.setColor(Color.BLACK);
        pushButton.attachChild(pushButtonCenter);
        pushButton.setVisible(false);

        meterObject = new Rectangle(ObjectDimensions.szMeterObjectPadding, ObjectDimensions.szMeterObjectPadding, 0 , ObjectDimensions.szMeterHeight - ObjectDimensions.szMeterObjectPadding * 2, vertexBufferObjectManager);
        meterObject.setColor(Color.WHITE);

        valueText = new Text(0, ObjectDimensions.szMeterHeight + 5, mFont.get("multiplier"), "Level 00", vertexBufferObjectManager);
        updateLevelText();
        valueText.setColor(Color.BLACK);

        attachChild(background);
        attachChild(pushButtonBackground);
        attachChild(meterObject);
        attachChild(pushButton);
        attachChild(valueText);
    }

    public int getHeight() {
        return ObjectDimensions.szMeterHeight + ObjectDimensions.szPushButtonSize;
    }

    public void setMaxunits(int maxunits) {
        this.maxunits = maxunits;
    }

    public void resetMeterState() {
        this.level = 1;
        this.currentState = 0;
        this.maxunits = 100;
        meterObject.setWidth(0);
        pushButton.setVisible(false);
        updateLevelText();
    }

    public void markStart()
    {
        this.prevPoints = currentState;
    }

    public void addPoints(int currentState) {
        if (this.currentState + currentState > maxunits) {
            this.currentState = maxunits;
            if (!isSuperActivated) {
                this.animatePending = true;
            }
        } else {
            this.currentState += currentState;
        }

    }

    public boolean use() {
        if (isSuperActivated) {
            isSuperActivated = false;
            meterObject.setWidth(0);
            pushButton.setVisible(false);
            maxunits += (maxunits / 4);
            currentState = 0;
            prevPoints = 0;
            level+=1;
            updateLevelText();
            return true;
        }
        return false;
    }

    private void updateLevelText() {
        valueText.setText("Level " + StringUtils.leftPad(Integer.toString(level), 2, " "));
    }

    private void activateSuper() {
        this.isSuperActivated = true;
        this.pushButton.setVisible(true);
        meterObject.setWidth(0);
        soundAssets.get("super").play();
    }

    public void animate() {
        if (animatePending || prevPoints!=this.currentState) {
            animatePending = false;
            float maxWidth = width - 4;
            float prevWidth = (maxWidth * prevPoints) / maxunits;
            float targetWidth = (maxWidth * this.currentState) / maxunits;
            meterObject.setWidth(prevWidth);
            Log.d(TAG, "animate to target " + this.currentState + " maxunits " + maxunits + " width " + targetWidth);
            meterObject.registerEntityModifier(new WidthModifier(1, targetWidth - prevWidth, new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    if (currentState >=maxunits) {
                        activateSuper();
                    }
                }
            }));
        }
    }


    @Override
    public void setWidth(float pValue) {
        meterObject.setWidth(pValue);
    }

    public boolean isAreaTouched(TouchEvent pSceneTouchEvent) {
        Log.d(TAG,"meter X " + pushButton.getX() + " Y " + pushButton.getY());
        float[] coordinates = convertLocalToSceneCoordinates(pushButton.getX(), pushButton.getY());

        Log.d(TAG,"meter X " + coordinates[Constants.VERTEX_INDEX_X] + " Y " + coordinates[Constants.VERTEX_INDEX_Y] );
        if ( coordinates[Constants.VERTEX_INDEX_X] <= pSceneTouchEvent.getX() &&  (coordinates[Constants.VERTEX_INDEX_X] + pushButton.getWidth()) >= pSceneTouchEvent.getX() &&
                coordinates[Constants.VERTEX_INDEX_Y] <= pSceneTouchEvent.getY() && (pSceneTouchEvent.getY() <= coordinates[Constants.VERTEX_INDEX_Y] + pushButton.getHeight()) ) {
            return true;
        }
        return false;
    }

    public boolean isSuperActive() {
        return isSuperActivated;
    }
}
