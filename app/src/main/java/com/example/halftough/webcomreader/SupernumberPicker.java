package com.example.halftough.webcomreader;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

public class SupernumberPicker extends NumberPicker {

    public SupernumberPicker(Context context) {
        super(context);
    }

    public SupernumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        createAtributes(attrs);
    }

    public SupernumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createAtributes(attrs);
    }

    private void createAtributes(AttributeSet attributeSet){
        setMinValue(attributeSet.getAttributeIntValue(null, "min", 0));
        setMaxValue(attributeSet.getAttributeIntValue(null, "max", 1));
    }
}
