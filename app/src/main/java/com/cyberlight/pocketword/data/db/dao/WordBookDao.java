package com.cyberlight.pocketword.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface WordBookDao {

    @Insert
    ListenableFuture<Long> insertWordBook(WordBook wordBook);

    @Insert
    long insertWordBookSync(WordBook wordBook);

    @Update
    ListenableFuture<Void> updateWordBook(WordBook wordBook);

    @Delete
    ListenableFuture<Void> deleteWordBook(WordBook wordBook);

    @Query("SELECT tb_wordbook.* FROM tb_settings " +
            "INNER JOIN tb_wordbook ON tb_settings.word_book_id = tb_wordbook.word_book_id")
    LiveData<WordBook> getUsingWordBookLiveData();

    @Query("SELECT tb_wordbook.* FROM tb_settings " +
            "INNER JOIN tb_wordbook ON tb_settings.word_book_id = tb_wordbook.word_book_id")
    WordBook getUsingWordBookSync();

    @Query("SELECT * FROM tb_wordbook")
    LiveData<List<WordBook>> getWordBooksLiveData();

}
