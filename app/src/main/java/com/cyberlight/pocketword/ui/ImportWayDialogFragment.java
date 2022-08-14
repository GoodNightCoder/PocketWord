package com.cyberlight.pocketword.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.cyberlight.pocketword.R;

public class ImportWayDialogFragment extends DialogFragment {
    public static final String TAG = "ImportWayDialogFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        // 该对话框dismiss会导致child对话框一起dismiss，
        // 为保证textImportDialogFragment不dismiss，
        // 此处必须getParentFragmentManager()而不是
        // getChildFragmentManager()
        FragmentManager parentFragmentManager = getParentFragmentManager();
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_import_way, null);
        TextView csvImportTv = view.findViewById(R.id.dialog_import_way_csv);
        TextView textImportTv = view.findViewById(R.id.dialog_import_way_text);
        csvImportTv.setOnClickListener(v -> {
            // 选择CSV文件导入词库
            Intent getCsvIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getCsvIntent.setType("*/*");
            getCsvIntent.addCategory(Intent.CATEGORY_OPENABLE);
            requireActivity().startActivityForResult(getCsvIntent, ImportActivity.REQUEST_COLLECT_CSV_OPEN);
            dismiss();
        });
        textImportTv.setOnClickListener(v -> {
            if (parentFragmentManager.findFragmentByTag(TextImportDialogFragment.TAG) == null) {
                DialogFragment dialogFragment = new TextImportDialogFragment();
                dialogFragment.show(parentFragmentManager, TextImportDialogFragment.TAG);
            }
            dismiss();
        });
        // 设置对话框
        Dialog dialog = new Dialog(context, R.style.FadeAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 贴近底部
            lp.y = 80;// 与底部的距离
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }
}