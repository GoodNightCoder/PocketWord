package com.cyberlight.pocketword.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cyberlight.pocketword.data.db.entity.WordBookWord;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface WordBookWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWordBookWordSync(WordBookWord wordBookWord);

    @Update
    void updateWordBookWordSync(WordBookWord wordBookWord);

    @Delete
    ListenableFuture<Void> deleteWordBookWords(List<WordBookWord> wordBookWords);

    @Query("SELECT * FROM tb_wordbook_word WHERE word_id = :wordId AND word_book_id = :wordBookId")
    WordBookWord getWordBookWordSync(long wordId, long wordBookId);
}