package com.cyberlight.pocketword.ui;

import android.app.Application;
import android.content.SharedPreferences;
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
                    Log.d(TAG, "usingWordBookLiveDatać›´ć–°:" + input);
                    return repository.getWordBookById(input);
                }
            });
    private final LiveData<List<CollectWord>> wordsLiveData = Transformations.switchMap(
            wordsMediatorLiveData, new Function<WordsMediatorData, LiveData<List<CollectWord>>>() {
                @Override
                public LiveData<List<CollectWord>> apply(WordsMediatorData input) {
                    Log.d(TAG, "wordsLiveDatać›´ć–°:\nusingWordBookId:" + input.usingWordBookId
                            + "\nuserSearch:" + input.userSearch);
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
        wordBooksLiveData = repository.getWordBooks();
        wordsMediatorLiveData.addSource(usingWordBookIdLiveData, aLong -> {
            WordsMediatorData data = wordsMediatorLiveData.getValue();
            if (data != null) {
                Log.d(TAG, "wordsMediatorLiveDatać›´ć–°:čŻŤäą¦");
                data.usingWordBookId = aLong;
                wordsMediatorLiveData.postValue(data);
            }
        });
        wordsMediatorLiveData.addSource(userSearchLiveData, s -> {
            if (s != null) {
                WordsMediatorData data = wordsMediatorLiveData.getValue();
                if (data != null) {
                    Log.d(TAG, "wordsMediatorLiveDatać›´ć–°:ć?śç´˘");
                    data.userSearch = s;
                    wordsMediatorLiveData.postValue(data);
                }
            }
        });
        // ĺ?ťĺ§‹čŽ·ĺŹ–ä˝żç”¨ä¸­čŻŤäą¦
        long usingWordBookId = prefsMgr.getUsingWordBookId();
        usingWordBookIdLiveData.postValue(usingWordBookId);
        // ĺ?ťĺ§‹ĺŚ–wordsMediatorLiveDatać•°ćŤ®
        wordsMediatorLiveData.postValue(new WordsMediatorData(usingWordBookId, ""));
    }

    // čż”ĺ›žĺŤ•čŻŤčˇ¨
    public LiveData<List<CollectWord>> getWords() {
        return wordsLiveData;
    }

    // čż”ĺ›žčŻŤäą¦ĺ?—čˇ¨
    public LiveData<List<WordBook>> getWordBooks() {
        return wordBooksLiveData;
    }

    // čż”ĺ›žé€‰ä¸­çš„čŻŤäą¦
    public LiveData<WordBook> getUsingWordBook() {
        return usingWordBookLiveData;
    }

    // ĺ?›ĺ»şĺą¶é€‰ä¸­čŻŤäą¦
    public void createAndUseWordBook(String wordBookName) {
        new Thread(() -> {
            long newWordBookId = repository.insertWordBookSync(new WordBook(wordBookName));
            prefsMgr.setUsingWordBookId(newWordBookId);
        }).start();
    }

    // ć?śç´˘ćˇ†čľ“ĺ…Ąĺ†…ĺ®ąć”ąĺŹ?ć—¶č°?ç”¨
    public void setUserSearch(String input) {
        userSearchLiveData.postValue(input);
    }

    public void useWordBook(WordBook wordBook) {
        prefsMgr.setUsingWordBookId(wordBook.getWordBookId());
    }

    public void deleteWordBook(WordBook wordBook) {
        repository.deleteWordBook(wordBook);
    }


    public LiveData<Boolean> isWorking() {
        return working;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PrefsConst.USING_WORD_BOOK_ID_KEY.equals(key)) {
            long usingWordBookId = prefsMgr.getUsingWordBookId();
            usingWordBookIdLiveData.postValue(usingWordBookId);
            Log.d(TAG, "ä˝żç”¨čŻŤäą¦IDć›´ć”ąä¸ş:" + usingWordBookId);
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
