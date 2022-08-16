package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.widget.IntegerWheelPicker;

public class SetGoalDialogFragment extends DialogFragment {
    public static final String TAG = "SetGoalDialogFragment";
    private static final String DAILY_GOAL_KEY = "daily_goal_key";
    private int mDailyGoal;

    public SetGoalDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        if (savedInstanceState != null) {
            mDailyGoal = savedInstanceState.getInt(DAILY_GOAL_KEY);
        } else {
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
            mDailyGoal = prefsMgr.getDailyGoal();
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_set_goal, null);
        IntegerWheelPicker goalWp = view.findViewById(R.id.dialog_set_goal_wp);
        goalWp.setSelectedValue(mDailyGoal, false);
        goalWp.setOnValueSelectedListener(value -> mDailyGoal = value);
        TextView confirmTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        TextView cancelTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        confirmTv.setText(R.string.dialog_confirm);
        cancelTv.setText(R.string.dialog_cancel);
        confirmTv.setOnClickListener(v -> {
            // 更改设置
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
            prefsMgr.setDailyGoal(mDailyGoal);
            dismiss();
        });
        cancelTv.setOnClickListener(v -> dismiss());
        // 设置对话框
        Dialog dialog = new Dialog(context, R.style.FadeAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 紧贴底部
            lp.y = 80;// 与底部距离
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(DAILY_GOAL_KEY, mDailyGoal);
        super.onSaveInstanceState(outState);
    }

}