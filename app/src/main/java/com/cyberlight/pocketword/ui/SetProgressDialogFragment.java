package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.cyberlight.pocketword.R;

public class SetProgressDialogFragment extends DialogFragment {
    public static final String TAG = "SetProgressDialogFragment";

    private int wordNumOfUsingBook;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ActivityMainViewModel model = new ViewModelProvider(this).get(ActivityMainViewModel.class);
        Context context = getContext();
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_set_progress, null);
        TextView titleTv = view.findViewById(R.id.dialog_set_progress_title_tv);
        EditText inputEt = view.findViewById(R.id.dialog_set_progress_content_tv);
        TextView negativeTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        TextView positiveTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        model.getUsingWordBook().observe(this, wordBook -> {
            if (wordBook != null) {
                titleTv.setText(getString(R.string.set_progress_dialog_title, wordBook.getLearningProgress() + 1));
            } else {
                dismiss();
            }
        });
        model.getWordNumOfUsingBook().observe(this, integer -> {
            wordNumOfUsingBook = integer;
            inputEt.setHint(getString(R.string.set_progress_dialog_hint, wordNumOfUsingBook));
        });
        positiveTv.setText(R.string.dialog_confirm);
        negativeTv.setText(R.string.dialog_cancel);
        positiveTv.setOnClickListener(v -> {
            String userInputStr = inputEt.getText().toString();
            if (TextUtils.isEmpty(userInputStr)) return;
            int userInputProgress = Integer.parseInt(userInputStr) - 1;
            if (userInputProgress > 0 || userInputProgress <= wordNumOfUsingBook) {
                model.setLearningProgress(userInputProgress);
                dismiss();
            }
        });
        negativeTv.setOnClickListener(v -> dismiss());
        // 设置对话框
        Dialog dialog = new Dialog(context, R.style.FadeAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }
}