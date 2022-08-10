package com.cyberlight.pocketword.ui;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.google.common.util.concurrent.FutureCallback;

import java.util.List;

public class ActivityMainViewModel extends AndroidViewModel {

    private static final String TAG = "ActivityMainViewModel";

    private final DataRepository repository;

    // 指示是否有任务在进行，以控制界面的进度条显示
    private final MutableLiveData<Boolean> working = new MutableLiveData<>();

    // 选中词书
    private final LiveData<WordBook> usingWordBookLiveData;

    // 选中词书的词汇数量
    private final LiveData<Integer> wordNumOfUsingBook;

    // 用户搜索框输入内容
    private final MutableLiveData<String> userSearchLiveData = new MutableLiveData<>();

    // 结合选中wordList和搜索框用户输入两个可观察数据源
    private final MediatorLiveData<WordsMediator> wordsMediatorLiveData = new MediatorLiveData<>();

    // 单词列表
    private final LiveData<List<CollectWord>> wordsLiveData = Transformations.switchMap(
            wordsMediatorLiveData, new Function<WordsMediator, LiveData<List<CollectWord>>>() {
                @Override
                public LiveData<List<CollectWord>> apply(WordsMediator input) {
                    return repository.getMatchWordsFromWordListLiveData(input.wordBookId, input.userSearch);
                }
            });

    // 全部词书
    private final LiveData<List<WordBook>> wordBooksLiveData;

    public ActivityMainViewModel(@NonNull Application application) {
        super(application);
        repository = DataRepository.getInstance(application);

        usingWordBookLiveData = repository.getUsingWordBookLiveData();
        wordNumOfUsingBook = Transformations.switchMap(usingWordBookLiveData,
                input -> repository.getWordNumOfBook(input.getWordBookId()));
        wordsMediatorLiveData.addSource(usingWordBookLiveData, wordBook -> {
            if (wordBook != null) {
                // 词库切换，更新单词集
                WordsMediator mediator = wordsMediatorLiveData.getValue();
                String userSearch = mediator != null ? mediator.userSearch : "";
                wordsMediatorLiveData.postValue(new WordsMediator(wordBook.getWordBookId(), userSearch));
            }
        });
        wordsMediatorLiveData.addSource(userSearchLiveData, s -> {
            if (!TextUtils.isEmpty(s)) {
                // 用户搜索框输入内容改变，更新单词集
                WordsMediator mediator = wordsMediatorLiveData.getValue();
                long wordBookId = mediator != null ? mediator.wordBookId : 0;
                wordsMediatorLiveData.postValue(new WordsMediator(wordBookId, s));
            }
        });
        wordBooksLiveData = repository.getWordBooksLiveData();
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

    // 创建词书
    public void createWordBook(String wordBookName) {
        repository.createWordBook(wordBookName);
    }

    // 搜索框输入内容改变时调用
    public void setUserSearch(String input) {
        userSearchLiveData.postValue(input);
    }

    // 更新学习进度
    public void setLearningProgress(int learningProgress) {
        WordBook usingWordBook = usingWordBookLiveData.getValue();
        if (usingWordBook != null) {
            usingWordBook.setLearningProgress(learningProgress);
            repository.updateWordBook(usingWordBook);
        }
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

    public LiveData<Integer> getWordNumOfUsingBook() {
        return wordNumOfUsingBook;
    }

    // 选择单词表
    public void useWordBook(long wordBookId) {
        repository.useWordBook(wordBookId);
    }

    public LiveData<Boolean> isWorking() {
        return working;
    }

    public static class WordsMediator {
        public final long wordBookId;
        public final String userSearch;

        private WordsMediator(long wordBookId, String userSearch) {
            this.wordBookId = wordBookId;
            this.userSearch = userSearch;
        }
    }

}
