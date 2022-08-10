package com.cyberlight.pocketword.data.db;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.data.db.entity.Record;
import com.cyberlight.pocketword.data.db.entity.Settings;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.data.db.entity.WordBookWord;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.List;

public class DataRepository {

    private static DataRepository sInstance;

    private final AppDatabase database;

    private DataRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataRepository(context);
        }
        return sInstance;
    }

    /**
     * 下载单词音频时用于更新audio字段
     */
    public void updateWord(Word word) {
        database.wordDao().updateWord(word);
    }

    /**
     * 获取指定词书中的匹配单词，app主页用
     */
    public LiveData<List<CollectWord>> getMatchWordsFromWordListLiveData(long wordBookId, String query) {
        String pattern = query.replaceAll("[^a-zA-Z ]", "") + "%";
        return database.collectWordDao().getMatchWordsFromWordBookLiveData(wordBookId, pattern);
    }

    /**
     * PlayActivity用
     *
     * @param wordBookId
     * @return
     */
    public List<CollectWord> getCollectWordsFromWordBookSync(long wordBookId) {
        return database.collectWordDao().getCollectWordsFromWordBookSync(wordBookId);
    }

    /**
     * 音频下载服务用
     */
    public List<Word> getWordsFromWordBookSync(long wordBookId) {
        return database.wordDao().getWordsFromWordBookSync(wordBookId);
    }

    /**
     * ImportActivity自动补全时用
     */
    public Word getMatchWordSync(String wordStr) {
        return database.wordDao().getMatchWordSync(wordStr);
    }

    /**
     * 异步获取用户正在使用的词书
     */
    public LiveData<WordBook> getUsingWordBookLiveData() {
        return database.wordBookDao().getUsingWordBookLiveData();
    }

    /**
     * 同步获取用户正在使用的词书
     */
    public WordBook getUsingWordBookSync() {
        return database.wordBookDao().getUsingWordBookSync();
    }

    /**
     * 获取全部词书
     */
    public LiveData<List<WordBook>> getWordBooksLiveData() {
        return database.wordBookDao().getWordBooksLiveData();
    }

    /**
     * 创建词书
     *
     * @param wordBookName
     */
    public void createWordBook(String wordBookName) {
        new Thread(() -> {
            long newWordBookId = database.wordBookDao().insertWordBookSync(new WordBook(wordBookName, 0));
            // TODO: 测试返回的id是否正确
            useWordBook(newWordBookId);
        }).start();
    }

    /**
     * 使用词书
     */
    public void useWordBook(long wordBookId) {
        database.settingsDao().insertSettings(new Settings(1, wordBookId));
    }

    /**
     * 更新书本
     */
    public void updateWordBook(WordBook wordBook) {
        database.wordBookDao().updateWordBook(wordBook);
    }

    /**
     * 将单词导入指定词书，app主页more菜单用
     */
    public void importWordToWordBook(long wordId, long wordBookId) {
        database.wordBookWordDao().insertWordBookWordSync(
                new WordBookWord(wordId, wordBookId, System.currentTimeMillis(), false)
        );
    }

    /**
     * 获取词书的词汇数
     */
    public LiveData<Integer> getWordNumOfBook(long wordBookId) {
        return database.wordBookWordDao().getWordNumOfBook(wordBookId);
    }

    /**
     * 构建词典用
     * fixme:去掉该方法，用预填充数据库代替
     */
    public void insertWords(Context context, List<Word> words, FutureCallback<Void> callback) {
        ListenableFuture<Void> future = database.wordDao().insertWords(words);
        Futures.addCallback(future, callback, context.getMainExecutor());
    }

    /**
     * PlayActivity用
     */
    public Record getRecordByDateSync(LocalDate date) {
        return database.recordDao().getRecordByDateSync(date);
    }

    public LiveData<List<Record>> getRecordsAfter(LocalDate date){
        return database.recordDao().getRecordsAfter(date);
    }

    public LiveData<List<Record>> getRecordsByStudyCount(int studyCount) {
        return database.recordDao().getRecordsByStudyCount(studyCount);
    }

    public void insertRecord(Record record) {
        database.recordDao().insertRecord(record);
    }

}
