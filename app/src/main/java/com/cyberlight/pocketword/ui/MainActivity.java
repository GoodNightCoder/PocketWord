package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.receiver.StudyRemindReceiver;
import com.cyberlight.pocketword.service.DownloadAudioService;
import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.util.TextWatcherAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_DICT_CSV_OPEN = 68;

    private ActivityMainViewModel mViewModel;

    private WordRecyclerAdapter mWordRecyclerAdapter;
    private WordBookRecyclerAdapter mWordBookRecyclerAdapter;

    private final List<CollectWord> mWords = new ArrayList<>();
    private final List<WordBook> mWordBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView wordBookTv = findViewById(R.id.main_word_book_tv);
        RecyclerView wordBooksRv = findViewById(R.id.main_word_books_rv);
        ImageView statisticsIv = findViewById(R.id.main_statistics_iv);
        ImageView settingsIv = findViewById(R.id.main_settings_iv);
        ImageView playIv = findViewById(R.id.main_play_iv);
        ImageView moreIv = findViewById(R.id.main_more_iv);
        EditText searchEt = findViewById(R.id.main_search_et);
        RecyclerView wordsRv = findViewById(R.id.main_words_rv);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewModel = new ViewModelProvider(this).get(ActivityMainViewModel.class);
        mWordRecyclerAdapter = new WordRecyclerAdapter(this, mWords);
        mWordBookRecyclerAdapter = new WordBookRecyclerAdapter(mWordBooks, new WordBookRecyclerAdapter.OnClickCallback() {
            @Override
            public void onChoose(WordBook wordBook) {
                // 选中词书
                mViewModel.useWordBook(wordBook.getWordBookId());
                wordBooksRv.setVisibility(View.GONE);
            }

            @Override
            public void onCreate() {
                // 创建词书
                if (fragmentManager.findFragmentByTag(CreateWordBookDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new CreateWordBookDialogFragment();
                    dialogFragment.show(getSupportFragmentManager(), CreateWordBookDialogFragment.TAG);
                    wordBooksRv.setVisibility(View.GONE);
                }
            }
        });

        searchEt.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) ->
                source.toString().replaceAll("[^a-zA-Z ]", "")});
        searchEt.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setUserSearch(s.toString());
            }
        });

        wordBookTv.setOnClickListener(view ->
                wordBooksRv.setVisibility(
                        wordBooksRv.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE));

        wordsRv.setLayoutManager(new LinearLayoutManager(this));
        wordsRv.setAdapter(mWordRecyclerAdapter);

        wordBooksRv.setLayoutManager(new LinearLayoutManager(this));
        wordBooksRv.setAdapter(mWordBookRecyclerAdapter);

        statisticsIv.setOnClickListener(v -> {
            Intent statisticsIntent = new Intent(this, StatisticsActivity.class);
            startActivity(statisticsIntent);
        });

        settingsIv.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        });

        playIv.setOnClickListener(v -> {
            Intent playIntent = new Intent(this, PlayActivity.class);
            startActivity(playIntent);
        });

        moreIv.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.main_more_import:
                        // 导入单词
                        Intent importIntent = new Intent(this, ImportActivity.class);
                        startActivity(importIntent);
                        return true;
                    case R.id.main_more_delete:
                        // fixme:
//                        setDeleteMode(true);
                        return true;
                    case R.id.main_more_download_audio:
                        // 启动服务下载单词音频
                        Intent downloadServiceIntent = new Intent(this, DownloadAudioService.class);
                        startService(downloadServiceIntent);
                        return true;
                    case R.id.main_more_build_dict:// fixme: 数据库设计完全后去除
                        // 选择CSV文件构建词典
                        Intent getDictCsvIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getDictCsvIntent.setType("*/*");
                        getDictCsvIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(getDictCsvIntent, REQUEST_DICT_CSV_OPEN);
                        return true;
                    default:
                        return false;
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.main_more_menu, popup.getMenu());
            popup.show();
        });

        mViewModel.getWords().observe(this, collectWords -> {
            if (collectWords != null) {
                Log.d(TAG, "刷新单词");
                mWords.clear();
                mWords.addAll(collectWords);
                mWordRecyclerAdapter.notifyDataSetChanged();
            }
        });
        mViewModel.getWordBooks().observe(this, wordBooks -> {
            if (wordBooks != null) {
                mWordBooks.clear();
                mWordBooks.addAll(wordBooks);
                mWordBookRecyclerAdapter.notifyDataSetChanged();
            }
        });
        mViewModel.getUsingWordBook().observe(this, wordBook -> {
            if (wordBook != null) {
                wordBookTv.setText(wordBook.getWordBookName());
            } else {
                wordBookTv.setText(R.string.main_no_book_selected);
            }
        });
        mViewModel.isWorking().observe(this, working ->
                progressBar.setVisibility(working ? View.VISIBLE : View.INVISIBLE)
        );

        // 检查学习提醒是否启动
        StudyRemindReceiver.checkReminder(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DICT_CSV_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            // 从csv文件导入单词到词典
            Uri uri = data.getData();
            if (uri != null) {
                List<Word> wordsToImport = new ArrayList<>();
                try {
                    // 读取csv文件词汇
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            getContentResolver().openInputStream(uri), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] arr = line.split("\\|");
                        if (arr.length == 0) continue;
                        String word = null, mean = null, accent = null;
                        for (int i = 0; i < arr.length; i++) {
                            switch (i) {
                                case 0:
                                    word = arr[i].trim();
                                    break;
                                case 1:
                                    mean = arr[i].trim();
                                    break;
                                case 2:
                                    accent = arr[i].trim();
                            }
                        }
                        if (!TextUtils.isEmpty(word) && !TextUtils.isEmpty(mean)) {
                            Word w = new Word(word, mean, accent, null);
                            wordsToImport.add(w);
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (wordsToImport.size() > 0) {
                    mViewModel.importWordsToDict(wordsToImport);
                }
            }
        }
    }

}