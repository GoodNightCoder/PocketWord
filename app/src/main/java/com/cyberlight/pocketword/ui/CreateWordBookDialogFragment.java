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

public class CreateWordBookDialogFragment extends DialogFragment {
    public static final String TAG = "CreateWordBookDialogFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ActivityMainViewModel model = new ViewModelProvider(requireActivity()).get(ActivityMainViewModel.class);
        Context context = requireContext();
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_create_wordbook, null);
        EditText inputEt = view.findViewById(R.id.dialog_create_wordbook_input_et);
        TextView negativeTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        TextView positiveTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        positiveTv.setText(R.string.dialog_confirm);
        negativeTv.setText(R.string.dialog_cancel);
        positiveTv.setOnClickListener(v -> {
            String userInputStr = inputEt.getText().toString().trim();
            if (TextUtils.isEmpty(userInputStr)) return;
            model.createAndUseWordBook(userInputStr);
            dismiss();
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
