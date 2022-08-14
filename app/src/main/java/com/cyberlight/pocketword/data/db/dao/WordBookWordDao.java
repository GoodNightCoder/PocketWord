package com.cyberlight.pocketword.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cyberlight.pocketword.data.db.entity.WordBookWord;
import com.google.common.util.concurrent.ListenableFuture;

@Dao
public interface WordBookWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWordBookWordSync(WordBookWord wordBookWord);

    @Update
    void updateWordBookWordSync(WordBookWord wordBookWord);

    @Query("SELECT * FROM tb_wordbook_word WHERE word_id = :wordId AND word_book_id = :wordBookId")
    WordBookWord getWordBookWordSync(long wordId,long wordBookId);
}