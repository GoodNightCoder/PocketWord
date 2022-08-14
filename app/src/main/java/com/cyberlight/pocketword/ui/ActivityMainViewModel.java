package com.cyberlight.pocketword.ui;

import android.app.Application;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.preference.PreferenceManager;

import com.cyberlight.pocketword.data.pref.PrefsConst;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.google.common.util.concurrent.FutureCallback;

import java.util.List;

public class ActivityMainViewModel extends AndroidViewModel
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ActivityMainViewModel";

    private final DataRepository repository;
    private final PrefsMgr prefsMgr;

    private final LiveData<List<WordBook>> wordBooksLiveData;
    private final MutableLiveData<Boolean> working = new MutableLiveData<>();

    private final MutableLiveData<Long> usingWordBookIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userSearchLiveData = new MutableLiveData<>();
    private final MediatorLiveData<WordsMediatorData> wordsMediatorLiveData = new MediatorLiveData<>();

    private final LiveData<WordBook> usingWordBookLiveData = Transformations.switchMap(
            usingWordBookIdLiveData, new Function<Long, LiveData<WordBook>>() {
                @Override
                public LiveData<WordBook> apply(Long input) {
                    Log.d(TAG, "usingWordBookLiveData更新:" + input);
                    return repository.getWordBookById(input);
                }
            });
    private final LiveData<List<CollectWord>> wordsLiveData = Transformations.switchMap(
            wordsMediatorLiveData, new Function<WordsMediatorData, LiveData<List<CollectWord>>>() {
                @Override
                public LiveData<List<CollectWord>> apply(WordsMediatorData input) {
                    Log.d(TAG, "wordsLiveData更新:\nusingWordBookId:"+input.usingWordBookId
                    +"\nuserSearch:"+input.userSearch);
                    String pattern = input.userSearch.replaceAll("[^a-zA-Z ]", "") + "%";
                    return repository.getCollectWords(input.usingWordBookId, pattern);
                }
            });


    @Override
    protected void onCleared() {
        PreferenceManager.getDefaultSharedPreferences(getApplication())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onCleared();
    }


    public ActivityMainViewModel(@NonNull Application application) {
        super(application);
        repository = DataRepository.getInstance(application);
        prefsMgr = SharedPrefsMgr.getInstance(application);
        PreferenceManager.getDefaultSharedPreferences(application)
                .registerOnSharedPreferenceChangeListener(this);
//        wordNumOfUsingBook = Transformations.switchMap(usingWordBookLiveData,
//                input -> repository.getWordNumOfBook(input.getWordBookId()));
        wordBooksLiveData = repository.getWordBooks();
        wordsMediatorLiveData.addSource(usingWordBookIdLiveData, aLong -> {
            WordsMediatorData data = wordsMediatorLiveData.getValue();
            if (data != null) {
                Log.d(TAG, "wordsMediatorLiveData更新:词书");
                data.usingWordBookId = aLong;
                wordsMediatorLiveData.postValue(data);
            }
        });
        wordsMediatorLiveData.addSource(userSearchLiveData, s -> {
            if (!TextUtils.isEmpty(s)) {
                WordsMediatorData data = wordsMediatorLiveData.getValue();
                if (data != null) {
                    Log.d(TAG, "wordsMediatorLiveData更新:搜索");
                    data.userSearch = s;
                    wordsMediatorLiveData.postValue(data);
                }
            }
        });
        // 初始获取使用中词书
        long usingWordBookId = prefsMgr.getUsingWordBookId();
        usingWordBookIdLiveData.postValue(usingWordBookId);
        // 初始化wordsMediatorLiveData数据
        wordsMediatorLiveData.postValue(new WordsMediatorData(usingWordBookId, ""));
    }

    // 返回单词表
    public LiveData<List<CollectWord>> getWords() {
        return wordsLiveData;
    }

    // 返回词书列表
    public LiveData<List<WordBook>> getWordBooks() {
        return wordBooksLiveData;
    }

    // 返回选中的词书
    public LiveData<WordBook> getUsingWordBook() {
        return usingWordBookLiveData;
    }

    // 创建并选中词书
    public void createAndUseWordBook(String wordBookName) {
        new Thread(() -> {
            long newWordBookId = repository.insertWordBookSync(new WordBook(wordBookName));
            prefsMgr.setUsingWordBookId(newWordBookId);
        }).start();
    }

    // 搜索框输入内容改变时调用
    public void setUserSearch(String input) {
        userSearchLiveData.postValue(input);
    }

    // 构建词典
    // fixme： 数据库设计完成后去除
    public void importWordsToDict(List<Word> words) {
        working.postValue(true);
        repository.insertWords(getApplication(), words, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                working.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                working.postValue(false);
            }
        });
    }

    public void useWordBook(long wordBookId) {
        prefsMgr.setUsingWordBookId(wordBookId);
    }

    public LiveData<Boolean> isWorking() {
        return working;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PrefsConst.USING_WORD_BOOK_ID_KEY.equals(key)) {
            long usingWordBookId = prefsMgr.getUsingWordBookId();
            usingWordBookIdLiveData.postValue(usingWordBookId);
            Log.d(TAG, "使用词书ID更改为:" + usingWordBookId);
        }
    }

    public static class WordsMediatorData {
        public long usingWordBookId;
        public String userSearch;

        public WordsMediatorData(long usingWordBookId, String userSearch) {
            this.usingWordBookId = usingWordBookId;
            this.userSearch = userSearch;
        }
    }

}
