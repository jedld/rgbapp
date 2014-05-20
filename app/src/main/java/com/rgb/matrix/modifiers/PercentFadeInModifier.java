package com.rgb.matrix.modifiers;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.SingleValueChangeEntityModifier;

/**
 * Created by joseph on 5/21/14.
 */
public class PercentFadeInModifier extends SingleValueChangeEntityModifier {

    private static final String TAG = HeightModifier.class.getName();
    private final float finalValue;
    private float startValue;
    private float orginalAlpha;

    public PercentFadeInModifier(float pDuration, float pValueChange, float finalValue, IEntityModifierListener listener) {
        super(pDuration, pValueChange, listener);
        startValue = 0;
        this.finalValue = finalValue;
    }

    @Override
    protected void onModifierStarted(IEntity pItem) {
        super.onModifierStarted(pItem);
        orginalAlpha = pItem.getAlpha();
        pItem.setAlpha(0f);
        startValue = 0;
    }

    @Override
    protected void onModifierFinished(IEntity pItem) {
        pItem.setAlpha(orginalAlpha);
        super.onModifierFinished(pItem);
    }

    @Override
    protected void onChangeValue(float pSecondsElapsed, IEntity pItem, float pValue) {
        startValue+=pValue;
        pItem.setAlpha(orginalAlpha * (startValue / 100f));
    }

    @Override
    public IEntityModifier deepCopy() throws DeepCopyNotSupportedException {
        throw new DeepCopyNotSupportedException();

    }
}
