package com.rgb.matrix.modifiers;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.SingleValueChangeEntityModifier;
import org.andengine.entity.primitive.Rectangle;

/**
 * Created by joseph on 5/21/14.
 */
public class PercentFadeOutModifier extends SingleValueChangeEntityModifier {

    private static final String TAG = HeightModifier.class.getName();
    private final float finalValue;
    private float orginalAlpha;
    private float startValue;

    public PercentFadeOutModifier(float pDuration, float pValueChange, float finalValue, IEntityModifierListener listener) {
        super(pDuration, pValueChange, listener);
        this.finalValue = finalValue;
    }

    @Override
    protected void onModifierStarted(IEntity pItem) {
        super.onModifierStarted(pItem);
        orginalAlpha = pItem.getAlpha();
        startValue = 100f;
    }

    @Override
    protected void onModifierFinished(IEntity pItem) {
        pItem.setAlpha(orginalAlpha * (finalValue/100f));
        super.onModifierFinished(pItem);
    }

    @Override
    protected void onChangeValue(float pSecondsElapsed, IEntity pItem, float pValue) {
        startValue-=pValue;
        pItem.setAlpha(orginalAlpha * (startValue / 100f));
    }

    @Override
    public IEntityModifier deepCopy() throws DeepCopyNotSupportedException {
        throw new DeepCopyNotSupportedException();

    }
}
