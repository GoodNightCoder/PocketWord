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

    @Query("SELECT * FROM tb_wordbook WHERE word_book_id = :wordBookId")
    WordBook getWordBookByIdSync(long wordBookId);

    @Query("SELECT * FROM tb_wordbook")
    LiveData<List<WordBook>> getWordBooks();

    @Query("SELECT * FROM tb_wordbook WHERE word_book_id = :wordBookId")
    LiveData<WordBook> getWordBookById(long wordBookId);

}
