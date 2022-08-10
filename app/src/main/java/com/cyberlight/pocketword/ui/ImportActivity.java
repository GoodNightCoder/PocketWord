package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.util.TransUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class ImportActivity extends AppCompatActivity {

    private static final int REQUEST_COLLECT_CSV_OPEN = 64;
    private ImportRecyclerAdapter mImportRecyclerAdapter;
    private final List<Word> mImportWordList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private DataRepository mRepository;
    private WordBook mUsingWordBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        mRepository = DataRepository.getInstance(this);
        // 获取控件
        mProgressBar = findViewById(R.id.import_pb);
        ImageView cancelIv = findViewById(R.id.import_cancel_iv);
        ImageView confirmIv = findViewById(R.id.import_confirm_iv);
        RecyclerView importRv = findViewById(R.id.import_words_rv);
        TextView autoCompleteTv = findViewById(R.id.import_auto_complete_tv);
        cancelIv.setOnClickListener(v -> finish());
        confirmIv.setOnClickListener(v -> {
            mProgressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                for (Word w : mImportWordList) {
                    // 要求word和mean不能为空
                    if (!TextUtils.isEmpty(w.getWordStr().trim())
                            && w.getMean() != null
                            && !TextUtils.isEmpty(w.getMean().trim())) {
                        if (w.getWordId() != -1) {
                            mRepository.importWordToWordBook(w.getWordId(), mUsingWordBook.getWordBookId());
                        } else {

                        }
                        // fixme: 还未能支持导入词库中没有的单词
                    }
                }
                runOnUiThread(() -> {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    finish();
                });
            }).start();
        });
        mImportRecyclerAdapter = new ImportRecyclerAdapter(mImportWordList);
        mImportRecyclerAdapter.setOnAddClickListener(() -> {
            Dialog importWayDialog = new Dialog(ImportActivity.this, R.style.FadeAnimDialog);
            View view = View.inflate(ImportActivity.this, R.layout.dialog_import_way, null);
            final TextView csvImportTv = view.findViewById(R.id.dialog_import_way_csv);
            final TextView textImportTv = view.findViewById(R.id.dialog_import_way_text);
            csvImportTv.setOnClickListener(v -> {
                importWayDialog.dismiss();
                // 选择CSV文件导入词库
                Intent getCsvIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getCsvIntent.setType("*/*");
                getCsvIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(getCsvIntent, REQUEST_COLLECT_CSV_OPEN);
            });
            textImportTv.setOnClickListener(v -> {
                importWayDialog.dismiss();
                // 创建对话框接收用户输入文本
                AlertDialog.Builder builder = new AlertDialog.Builder(ImportActivity.this);
                View view2 = View.inflate(ImportActivity.this, R.layout.dialog_text_import, null);
                final EditText et = view2.findViewById(R.id.dialog_text_import_et);
                final Button btn = view2.findViewById(R.id.dialog_text_import_btn);
                builder.setTitle(R.string.dialog_text_import_title).setIcon(R.drawable.ic_baseline_add_24).setView(view2);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                btn.setOnClickListener(v1 -> {
                    alertDialog.dismiss();
                    mProgressBar.setVisibility(View.VISIBLE);
                    new Thread(() -> {
                        String[] wordArr = et.getText().toString().split(",");
                        for (String word : wordArr) {
                            String wordStr = word.trim();
                            if (TextUtils.isEmpty(wordStr)) continue;
                            Word w = mRepository.getMatchWordSync(wordStr);
                            if (w == null) {
                                w = new Word(-1, wordStr, null, null, null);
                            }
                            mImportWordList.add(w);
                        }
                        runOnUiThread(() -> {
                            mImportRecyclerAdapter.notifyDataSetChanged();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        });
                    }).start();
                });
            });
            importWayDialog.setContentView(view);
            Window window = importWayDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.gravity = Gravity.BOTTOM; // 贴近底部
                lp.y = 80;// 与底部的距离
                lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
                window.setAttributes(lp);
            }
            importWayDialog.show();
        });
        importRv.setLayoutManager(new LinearLayoutManager(this));
        importRv.setAdapter(mImportRecyclerAdapter);
        autoCompleteTv.setOnClickListener(v -> {
            // 开始自动补全，自动补全期间autoCompleteMeanTv
            // 要将设为enabled=false，保证用户不能点击
            mProgressBar.setVisibility(View.VISIBLE);
            autoCompleteTv.setEnabled(false);
            new Thread(() -> {
                for (int i = 0; i < mImportWordList.size(); i++) {
                    Word w = mImportWordList.get(i);
                    autoComplete(w);
                    runOnUiThread(() -> mImportRecyclerAdapter.notifyDataSetChanged());
                }
                runOnUiThread(() -> {
                    autoCompleteTv.setEnabled(true);
                    mImportRecyclerAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                });
            }).start();
        });

        new Thread(() -> {
            mUsingWordBook = mRepository.getUsingWordBookSync();
            if (mUsingWordBook == null) {
                finish();
            }
        }).start();
        // 用户用本应用打开csv文件，进行csv单词导入
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                importWordsFromCSV(uri);
                runOnUiThread(() -> {
                    mImportRecyclerAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                });
            }).start();
        }
    }

    /**
     * 自动补全导入列表中的单词
     * 注：不能在主线程运行该方法
     */
    private void autoComplete(Word w) {
        String queryWordStr = w.getWordStr().trim();
        if (TextUtils.isEmpty(queryWordStr)) return;// 要求word非空
        // 补全释义
        if (w.getMean() == null || TextUtils.isEmpty(w.getMean().trim())) {
            w.setMean(TransUtil.translate(queryWordStr, "en", "zh"));// 联网翻译补全
        }
        // fixme: 无法联网补全音标
    }

    private void importWordsFromCSV(Uri uri) {
        if (uri == null) return;
        // 读取csv文件词汇
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    getContentResolver().openInputStream(uri), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("\\|");
                if (arr.length == 0) continue;
                String wordStr = null, mean = null, accent = null;
                for (int i = 0; i < arr.length; i++) {
                    switch (i) {
                        case 0:
                            wordStr = arr[i].trim();
                            break;
                        case 1:
                            mean = arr[i].trim();
                            break;
                        case 2:
                            accent = arr[i].trim();
                    }
                }
                if (!TextUtils.isEmpty(wordStr)) {
                    Word w = mRepository.getMatchWordSync(wordStr);
                    if (w == null) {
                        w = new Word(-1, wordStr, mean, accent, null);
                    }
                    mImportWordList.add(w);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_COLLECT_CSV_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            // 从csv文件导入单词到收藏词库
            Uri uri = data.getData();
            mProgressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                importWordsFromCSV(uri);
                runOnUiThread(() -> {
                    mImportRecyclerAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                });
            }).start();
        }
    }
}