package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.entity.Word;

@SuppressLint("NotifyDataSetChanged")
public class ImportActivity extends AppCompatActivity
        implements ConfirmImportDialogFragment.NoticeDialogListener {

    public static final int REQUEST_COLLECT_CSV_OPEN = 64;
    private ImportRecyclerAdapter mImportRecyclerAdapter;
    private ActivityImportViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        ProgressBar progressBar = findViewById(R.id.import_pb);
        ImageView cancelIv = findViewById(R.id.import_cancel_iv);
        ImageView confirmIv = findViewById(R.id.import_confirm_iv);
        RecyclerView importRv = findViewById(R.id.import_words_rv);
        TextView autoCompleteTv = findViewById(R.id.import_auto_complete_tv);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewModel = new ViewModelProvider(this).get(ActivityImportViewModel.class);

        mViewModel.isWorking().observe(this, working -> {
                    progressBar.setVisibility(working ? View.VISIBLE : View.INVISIBLE);
                    autoCompleteTv.setEnabled(!working);
                    confirmIv.setEnabled(!working);
                }
        );
        cancelIv.setOnClickListener(v -> finish());
        confirmIv.setOnClickListener(v -> {
            boolean conflict = false;
            for (int i = 0; i < mViewModel.importWords.size(); i++) {
                Word w = mViewModel.importWords.get(i);
                if (w.getWordId() != -1) conflict = true;
                if (w.getMean() == null || TextUtils.isEmpty(w.getMean().trim())) {
                    Toast.makeText(this,
                            getString(R.string.invalid_meaning_toast, w.getWordStr()),
                            Toast.LENGTH_SHORT).show();
                    importRv.scrollToPosition(i);
                    return;
                }
            }
            if (conflict) {
                if (fragmentManager.findFragmentByTag(ConfirmImportDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new ConfirmImportDialogFragment();
                    dialogFragment.show(fragmentManager, ConfirmImportDialogFragment.TAG);
                }
            } else {
                mViewModel.importWordsToDb(false);
            }
        });
        mImportRecyclerAdapter = new ImportRecyclerAdapter(this, mViewModel.importWords);
        mImportRecyclerAdapter.setOnAddClickListener(() -> {
            if (fragmentManager.findFragmentByTag(ImportWayDialogFragment.TAG) == null) {
                DialogFragment dialogFragment = new ImportWayDialogFragment();
                dialogFragment.show(fragmentManager, ImportWayDialogFragment.TAG);
            }
        });
        importRv.setLayoutManager(new LinearLayoutManager(this));
        importRv.setAdapter(mImportRecyclerAdapter);
        autoCompleteTv.setOnClickListener(v -> {
            // fixme:开始自动补全，自动补全期间autoCompleteMeanTv要设enabled=false，保证用户不能点击
            mViewModel.autoComplete();
        });
        mViewModel.setOnChangedListener(new ActivityImportViewModel.OnChangedListener() {
            @Override
            public void onDataSetChanged() {
                runOnUiThread(() -> mImportRecyclerAdapter.notifyDataSetChanged());
            }

            @Override
            public void onImported() {
                finish();
            }
        });
        // 用户用本应用打开csv文件，进行csv单词导入
        Intent intent = getIntent();
        Uri uri = intent.getData();
        mViewModel.importWordsFromCSV(uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_COLLECT_CSV_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            // 从csv文件导入单词到收藏词库
            Uri uri = data.getData();
            mViewModel.importWordsFromCSV(uri);
        }
    }

    @Override
    public void onDialogPositiveClick() {
        mViewModel.importWordsToDb(true);
    }

    @Override
    public void onDialogNegativeClick() {
        mViewModel.importWordsToDb(false);
    }
}