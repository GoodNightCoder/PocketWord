package com.cyberlight.pocketword.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CheckOnceButton extends androidx.appcompat.widget.AppCompatButton implements Checkable {
    private static final String TAG = "CheckOnceButton";
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private boolean mCheck;

    public CheckOnceButton(@NonNull Context context) {
        super(context);
    }

    public CheckOnceButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckOnceButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mCheck != checked) {
            mCheck = checked;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mCheck);
            }
        }
    }

    @Override
    public boolean isChecked() {
        return mCheck;
    }

    @Override
    public void toggle() {
        // to prevent toggle when the button is already checked
        Log.d(TAG, String.valueOf(isChecked()));
        if (!isChecked()) {
            setChecked(true);
        }
    }

    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(CheckOnceButton buttonView, boolean isChecked);
    }
}
