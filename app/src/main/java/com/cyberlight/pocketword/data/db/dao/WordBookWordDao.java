package com.cyberlight.pocketword.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cyberlight.pocketword.data.db.entity.WordBookWord;

@Dao
public interface WordBookWordDao {

    @Insert
    void insertWordBookWordSync(WordBookWord wordBookWord);

    @Query("SELECT COUNT(*) FROM tb_wordbook_word WHERE word_book_id = :wordBookId")
    LiveData<Integer> getWordNumOfBook(long wordBookId);
}