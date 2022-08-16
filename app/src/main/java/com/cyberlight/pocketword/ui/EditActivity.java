package com.cyberlight.pocketword.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.data.db.entity.WordBookWord;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.model.CollectWord;

import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private final List<CollectWord> mWords = new ArrayList<>();
    private final List<Boolean> mSelectStates = new ArrayList<>();
    private EditWordRecyclerAdapter mEditWordRecyclerAdapter;
    private WordBook mUsingWordBook;
    private DataRepository mRepository;
    private boolean mAllSelected = false;
    private boolean mSelectedAllKnown = false;
    private TextView mSelectAllTv;
    private TextView mSetKnownTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ImageView backIv = findViewById(R.id.edit_back_iv);
        mSelectAllTv = findViewById(R.id.edit_select_all_tv);
        mSetKnownTv = findViewById(R.id.edit_set_known_tv);
        TextView deleteTv = findViewById(R.id.edit_delete_tv);
        RecyclerView editWordsRv = findViewById(R.id.edit_word_rv);
        backIv.setOnClickListener(v -> finish());
        mSelectAllTv.setOnClickListener(v -> {
            for (int i = 0; i < mSelectStates.size(); i++) {
                if (mSelectStates.get(i) == mAllSelected) {
                    mSelectStates.set(i, !mAllSelected);
                    mEditWordRecyclerAdapter.notifyItemChanged(i);
                }
            }
            updateSelectAll();
        });
        mSetKnownTv.setOnClickListener(v -> {
            for (int i = 0; i < mWords.size(); i++) {
                boolean selected = mSelectStates.get(i);
                if (selected) {
                    CollectWord word = mWords.get(i);
                    if (mSelectedAllKnown) {
                        word.setKnown(false);
                        mEditWordRecyclerAdapter.notifyItemChanged(i);
                        new Thread(() -> {
                            WordBookWord wordBookWord = mRepository.getWordBookWordSync(
                                    word.getWordId(), mUsingWordBook.getWordBookId());
                            wordBookWord.setKnown(false);
                            mRepository.updateWordBookWordSync(wordBookWord);
                        }).start();
                    } else if (!word.isKnown()) {
                        word.setKnown(true);
                        mEditWordRecyclerAdapter.notifyItemChanged(i);
                        new Thread(() -> {
                            WordBookWord wordBookWord = mRepository.getWordBookWordSync(
                                    word.getWordId(), mUsingWordBook.getWordBookId());
                            wordBookWord.setKnown(true);
                            mRepository.updateWordBookWordSync(wordBookWord);
                        }).start();
                    }
                }
            }
            updateSelectAll();
        });
        deleteTv.setOnClickListener(v -> {
            List<WordBookWord> wordBookWordsToDelete = new ArrayList<>();
            for (int i = 0; i < mWords.size(); i++) {
                boolean selected = mSelectStates.get(i);
                if (selected) {
                    CollectWord word = mWords.get(i);
                    wordBookWordsToDelete.add(new WordBookWord(word.getWordId(), mUsingWordBook.getWordBookId()));
                    mWords.remove(i);
                    mSelectStates.remove(i);
                    i--;
                }
            }
            if (wordBookWordsToDelete.size() > 0) {
                mRepository.deleteWordBookWords(wordBookWordsToDelete);
            }
            updateSelectAll();
            mEditWordRecyclerAdapter.notifyDataSetChanged();
        });
        mEditWordRecyclerAdapter = new EditWordRecyclerAdapter(mWords, mSelectStates,
                (position, isChecked) -> {
                    mSelectStates.set(position, isChecked);
                    updateSelectAll();
                });
        editWordsRv.setLayoutManager(new LinearLayoutManager(this));
        editWordsRv.setAdapter(mEditWordRecyclerAdapter);
        mRepository = DataRepository.getInstance(this);
        // 加载数据
        new Thread(() -> {
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(this);
            // 获取并检查使用中词书
            long usingWordBookId = prefsMgr.getUsingWordBookId();
            mUsingWordBook = mRepository.getWordBookByIdSync(usingWordBookId);
            if (mUsingWordBook == null) {
                finish();
                return;
            }
            // 获取并检查单词表
            List<CollectWord> words = mRepository.getCollectWordsSync(mUsingWordBook.getWordBookId());
            if (words == null || words.size() == 0) {
                finish();
                return;
            }
            mWords.addAll(words);
            for (int i = 0; i < mWords.size(); i++) {
                mSelectStates.add(false);
            }
            updateSelectAll();
            runOnUiThread(() -> mEditWordRecyclerAdapter.notifyDataSetChanged());
        }).start();
    }

    private void updateSelectAll() {
        mAllSelected = true;
        mSelectedAllKnown = true;
        for (int i = 0; i < mWords.size(); i++) {
            boolean selected = mSelectStates.get(i);
            if (!selected) {
                mAllSelected = false;
            } else {
                CollectWord word = mWords.get(i);
                if (!word.isKnown()) {
                    mSelectedAllKnown = false;
                }
            }
        }
        mSelectAllTv.setText(mAllSelected ? getString(R.string.edit_unselect_all) : getString(R.string.edit_select_all));
        mSetKnownTv.setText(mSelectedAllKnown ? getString(R.string.edit_set_unknown) : getString(R.string.edit_set_known));
    }
}