package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.pocketword.R;

public class ConfirmImportDialogFragment extends DialogFragment {
    public static final String TAG = "ConfirmImportDialogFragment";

    public interface NoticeDialogListener {
        void onDialogPositiveClick();

        void onDialogNegativeClick();
    }

    private NoticeDialogListener mNoticeDialogListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mNoticeDialogListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Import activity must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_confirm_import, null);
        TextView negativeTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        TextView positiveTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        positiveTv.setText(R.string.dialog_yes);
        negativeTv.setText(R.string.dialog_no);
        positiveTv.setOnClickListener(v -> {
            mNoticeDialogListener.onDialogPositiveClick();
            dismiss();
        });
        negativeTv.setOnClickListener(v -> {
            mNoticeDialogListener.onDialogNegativeClick();
            dismiss();
        });
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
