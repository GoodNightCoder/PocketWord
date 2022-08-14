package com.cyberlight.pocketword.ui;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.data.db.entity.WordBookWord;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.util.TransUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ActivityImportViewModel extends AndroidViewModel {

    private static final String TAG = "ActivityImportViewModel";
    public final List<Word> importWords = new ArrayList<>();
    private final DataRepository repository;
    private WordBook usingWordBook;
    private final MutableLiveData<Boolean> workingLiveData = new MutableLiveData<>();
    private OnChangedListener onChangedListener;


    public ActivityImportViewModel(@NonNull Application application) {
        super(application);
        repository = DataRepository.getInstance(application);

        // 使用中词书只需单次获取即可
        new Thread(() -> {
            long usingWordBookId = SharedPrefsMgr.getInstance(application).getUsingWordBookId();
            usingWordBook = repository.getWordBookByIdSync(usingWordBookId);
        }).start();
    }

    public void importWordsFromCSV(Uri uri) {
        if (uri == null) return;
        new Thread(() -> {
            setWorking(true);
            importWords.clear();
            // 读取csv文件词汇
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        getApplication().getContentResolver().openInputStream(uri),
                        StandardCharsets.UTF_8));
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
                        Word w = repository.getMatchWordSync(wordStr);
                        if (w == null) {
                            w = new Word(-1, wordStr, mean, accent, null);
                        }
                        importWords.add(w);
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (onChangedListener != null) {
                onChangedListener.onDataSetChanged();
            }
            setWorking(false);
        }).start();
    }

    public void importWordsFromText(String text) {
        if (TextUtils.isEmpty(text)) return;
        new Thread(() -> {
            setWorking(true);
            importWords.clear();
            String[] wordArr = text.split(",");
            for (String word : wordArr) {
                String wordStr = word.trim();
                if (TextUtils.isEmpty(wordStr)) continue;
                Word w = repository.getMatchWordSync(wordStr);
                if (w == null) {
                    w = new Word(-1, wordStr, null, null, null);
                }
                importWords.add(w);
            }
            if (onChangedListener != null) {
                onChangedListener.onDataSetChanged();
            }
            setWorking(false);
        }).start();
    }

    public void autoComplete() {
        new Thread(() -> {
            setWorking(true);
            boolean changed = false;
            for (int i = 0; i < importWords.size(); i++) {
                Word w = importWords.get(i);
                String queryWordStr = w.getWordStr().trim();
                if (TextUtils.isEmpty(queryWordStr)) continue;// 要求word非空
                // 补全释义
                if (w.getMean() == null || TextUtils.isEmpty(w.getMean().trim())) {
                    changed = true;
                    w.setMean(TransUtil.translate(queryWordStr, "en", "zh"));// 联网翻译补全
                }
                // fixme: 无法联网补全音标
            }
            if (changed && onChangedListener != null) {
                onChangedListener.onDataSetChanged();
            }
            setWorking(false);
        }).start();
    }

    public void importWordsToDb(boolean replace) {
        if (usingWordBook != null) {
            new Thread(() -> {
                setWorking(true);
                for (Word w : importWords) {
                    if (w.getMean() != null && !TextUtils.isEmpty(w.getMean().trim())) {
                        if (w.getWordId() != -1) {
                            if (replace) {
                                // 由于WordBookWord的外键约束，此处不能用
                                // @Insert(onConflict=REPLACE)来更新词库单词，
                                // 否则会触发onDelete=CASCADE，从所有词书中删除该单词
                                repository.updateWordSync(w);
                            }
                            repository.insertWordBookWord(new WordBookWord(
                                    w.getWordId(),
                                    usingWordBook.getWordBookId(),
                                    System.currentTimeMillis()));
                        } else {
                            long wordId = repository.insertWordSync(w);
                            repository.insertWordBookWord(new WordBookWord(
                                    wordId,
                                    usingWordBook.getWordBookId(),
                                    System.currentTimeMillis()));
                        }
                    }
                }
                if (onChangedListener != null) {
                    onChangedListener.onImported();
                }
                setWorking(false);
            }).start();
        }
    }

    public void setWorking(boolean working) {
        workingLiveData.postValue(working);
    }

    public LiveData<Boolean> isWorking() {
        return workingLiveData;
    }

    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    public interface OnChangedListener {
        void onDataSetChanged();

        void onImported();
    }

}
