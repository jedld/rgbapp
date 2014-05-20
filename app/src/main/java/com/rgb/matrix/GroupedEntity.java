package com.rgb.matrix;

import com.rgb.matrix.modifiers.PercentFadeInModifier;
import com.rgb.matrix.modifiers.PercentFadeOutModifier;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.util.modifier.IModifier;

import java.util.ArrayList;

/**
 * Created by joseph on 5/21/14.
 */
public class GroupedEntity extends Entity {

    public GroupedEntity(float pX, float pY) {
        super(pX, pY);
    }

    public void setAlpha(float pAlpha) {
        for(int i = 0; i < getChildCount(); i++) {
            IEntity child = getChildByIndex(i);
            child.setAlpha(pAlpha);
        }
    }

    ArrayList<Float> originalAlphaVaues = new ArrayList<Float>();

    public void saveAlphas() {
        originalAlphaVaues.clear();
        for(int i = 0; i < getChildCount(); i++) {
            IEntity child = getChildByIndex(i);
            originalAlphaVaues.add(child.getAlpha());
        }
    }

    @Override
    public void attachChild(IEntity entity) {
        originalAlphaVaues.add(entity.getAlpha());
        super.attachChild(entity);
    }

    public void restoreAlphas() {
        for(int i = 0; i < getChildCount(); i++) {
            IEntity child = getChildByIndex(i);
            child.setAlpha(originalAlphaVaues.get(i));
        }
    }

    public void mark() {
        saveAlphas();
    }

    public void hide() {
        if (isVisible()) {
            restoreAlphas();
            PercentFadeOutModifier fadeOutModifier = new PercentFadeOutModifier(0.5f, 100f, 0f, new IEntityModifier.IEntityModifierListener() {
                @Override
                public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

                }

                @Override
                public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                    setVisible(false);
                }
            });
            applyModifierToChildren(fadeOutModifier);
        }
    }

    private void applyModifierToChildren(IEntityModifier modifier) {
        for(int i = 0; i < getChildCount(); i++) {
            IEntity child = getChildByIndex(i);
            if (child instanceof GroupedEntity) {
                ((GroupedEntity)child).applyModifierToChildren(modifier);
            } else {
                child.registerEntityModifier(modifier);
            }
        }
    }

    public void show() {
        if (!isVisible()) {
            restoreAlphas();
            setVisible(true);
        }
    }
}
