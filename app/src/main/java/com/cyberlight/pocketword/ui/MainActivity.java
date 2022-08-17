package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

    private ActivityMainViewModel mViewModel;

    private WordRecyclerAdapter mWordRecyclerAdapter;
    private WordBookRecyclerAdapter mWordBookRecyclerAdapter;

    private final List<CollectWord> mWords = new ArrayList<>();
    private final List<WordBook> mWordBooks = new ArrayList<>();
    private WordBook mUsingWordBook;

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
                mViewModel.useWordBook(wordBook);
            }

            @Override
            public void onCreate() {
                // 创建词书
                if (fragmentManager.findFragmentByTag(CreateWordBookDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new CreateWordBookDialogFragment();
                    dialogFragment.show(getSupportFragmentManager(), CreateWordBookDialogFragment.TAG);
                }
            }

            @Override
            public void onDelete(WordBook wordBook) {
                // 删除词书
                mViewModel.deleteWordBook(wordBook);
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
            if (mUsingWordBook == null) {
                Toast.makeText(this, getString(R.string.main_no_book_toast), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent playIntent = new Intent(this, PlayActivity.class);
            startActivity(playIntent);
        });

        moreIv.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.main_more_import:
                        // 导入单词
                        if (mUsingWordBook == null) {
                            Toast.makeText(this, getString(R.string.main_no_book_toast), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        Intent importIntent = new Intent(this, ImportActivity.class);
                        startActivity(importIntent);
                        return true;
                    case R.id.main_more_edit:
                        // 编辑单词
                        if (mUsingWordBook == null) {
                            Toast.makeText(this, getString(R.string.main_no_book_toast), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        Intent editIntent = new Intent(this, EditActivity.class);
                        startActivity(editIntent);
                        return true;
                    case R.id.main_more_download_audio:
                        // 启动服务下载单词音频
                        if (mUsingWordBook == null) {
                            Toast.makeText(this, getString(R.string.main_no_book_toast), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        Intent downloadServiceIntent = new Intent(this, DownloadAudioService.class);
                        startService(downloadServiceIntent);
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
            mUsingWordBook = wordBook;
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


}