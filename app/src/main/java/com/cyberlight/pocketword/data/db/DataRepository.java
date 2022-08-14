package com.cyberlight.pocketword.data.db;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Query;

import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.data.db.entity.Record;
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

    public long insertWordSync(Word word) {
        return database.wordDao().insertWordSync(word);
    }

    public void updateWordSync(Word word) {
        database.wordDao().updateWordSync(word);
    }

    public void updateWord(Word word) {
        database.wordDao().updateWord(word);
    }

    public LiveData<List<CollectWord>> getCollectWords(long wordBookId, String pattern) {
        return database.collectWordDao().getCollectWords(wordBookId, pattern);
    }

    public List<CollectWord> getCollectWordsSync(long wordBookId) {
        return database.collectWordDao().getCollectWordsSync(wordBookId);
    }

    public List<Word> getWordsFromBookSync(long wordBookId) {
        return database.wordDao().getWordsFromBookSync(wordBookId);
    }

    public Word getMatchWordSync(String wordStr) {
        return database.wordDao().getMatchWordSync(wordStr);
    }

    public LiveData<WordBook> getWordBookById(long wordBookId) {
        return database.wordBookDao().getWordBookById(wordBookId);
    }

    public WordBook getWordBookByIdSync(long wordBookId) {
        return database.wordBookDao().getWordBookByIdSync(wordBookId);
    }

    public LiveData<List<WordBook>> getWordBooks() {
        return database.wordBookDao().getWordBooks();
    }

    public long insertWordBookSync(WordBook wordBook) {
        return database.wordBookDao().insertWordBookSync(wordBook);
    }

    public void updateWordBook(WordBook wordBook) {
        database.wordBookDao().updateWordBook(wordBook);
    }

    public void insertWordBookWord(WordBookWord wordBookWord) {
        database.wordBookWordDao().insertWordBookWordSync(wordBookWord);
    }

    public void updateWordBookWordSync(WordBookWord wordBookWord) {
        database.wordBookWordDao().updateWordBookWordSync(wordBookWord);
    }

    public WordBookWord getWordBookWordSync(long wordId, long wordBookId) {
        return database.wordBookWordDao().getWordBookWordSync(wordId, wordBookId);
    }

    /**
     * 构建词典用
     * fixme:去掉该方法，用预填充数据库代替
     */
    public void insertWords(Context context, List<Word> words, FutureCallback<Void> callback) {
        ListenableFuture<Void> future = database.wordDao().insertWords(words);
        Futures.addCallback(future, callback, context.getMainExecutor());
    }

    public Record getRecordByDateSync(LocalDate date) {
        return database.recordDao().getRecordByDateSync(date);
    }

    public LiveData<List<Record>> getRecordsAfter(LocalDate date) {
        return database.recordDao().getRecordsAfter(date);
    }

    public LiveData<List<Record>> getRecordsByStudyCount(int studyCount) {
        return database.recordDao().getRecordsByStudyCount(studyCount);
    }

    public void insertRecord(Record record) {
        database.recordDao().insertRecord(record);
    }

    public void updateRecord(Record record) {
        database.recordDao().updateRecord(record);
    }

}
