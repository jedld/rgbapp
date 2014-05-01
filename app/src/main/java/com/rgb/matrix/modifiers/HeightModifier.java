package com.rgb.matrix.modifiers;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.SingleValueChangeEntityModifier;
import org.andengine.entity.primitive.Rectangle;

/**
 * Created by joseph on 5/1/14.
 */
public class HeightModifier extends SingleValueChangeEntityModifier {


    private static final String TAG = HeightModifier.class.getName();

    public HeightModifier(float pDuration, float pValueChange, IEntityModifierListener listener) {
        super(pDuration, pValueChange, listener);
    }

    @Override
    protected void onChangeValue(float pSecondsElapsed, IEntity pItem, float pValue) {
        Rectangle resizable = (Rectangle)pItem;
        resizable.setHeight(resizable.getHeight() + pValue);
    }

    @Override
    public IEntityModifier deepCopy() throws DeepCopyNotSupportedException {
        throw new DeepCopyNotSupportedException();

    }
}

