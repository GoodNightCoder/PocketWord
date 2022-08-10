package com.cyberlight.pocketword.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class ToggleRadioButton extends androidx.appcompat.widget.AppCompatRadioButton {

    public ToggleRadioButton(Context context) {
        super(context);
    }

    public ToggleRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }
}
