package com.rgb.matrix.modifiers;

import android.util.Log;

import com.rgb.matrix.interfaces.Resizable;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.EntityModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.SingleValueChangeEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.util.modifier.IModifier;

/**
 * Created by joseph on 5/1/14.
 */
public class WidthModifier extends SingleValueChangeEntityModifier {


    private static final String TAG = WidthModifier.class.getName();

    public WidthModifier(float pDuration, float pValueChange, IEntityModifierListener listener) {
        super(pDuration, pValueChange, listener);
    }

    @Override
    protected void onChangeValue(float pSecondsElapsed, IEntity pItem, float pValue) {
        Rectangle resizable = (Rectangle)pItem;
        resizable.setWidth(resizable.getWidth() + pValue);
    }

    @Override
    public IEntityModifier deepCopy() throws DeepCopyNotSupportedException {
        throw new DeepCopyNotSupportedException();

    }
}
