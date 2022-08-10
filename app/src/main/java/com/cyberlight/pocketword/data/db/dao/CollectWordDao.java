package com.cyberlight.pocketword.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.cyberlight.pocketword.model.CollectWord;

import java.util.List;

@Dao
public interface CollectWordDao {

    @Query("SELECT tb_word.*,import_at,known FROM tb_wordbook_word " +
            "INNER JOIN tb_wordbook ON tb_wordbook_word.word_book_id = tb_wordbook.word_book_id " +
            "INNER JOIN tb_word ON tb_wordbook_word.word_id = tb_word.word_id " +
            "WHERE tb_wordbook_word.word_book_id = :wordBookId AND tb_word.word_str LIKE :pattern " +
            "ORDER BY tb_word.word_str ASC")
    LiveData<List<CollectWord>> getMatchWordsFromWordBookLiveData(long wordBookId, String pattern);

    @Query("SELECT tb_word.*,import_at,known FROM tb_wordbook_word " +
            "INNER JOIN tb_wordbook ON tb_wordbook_word.word_book_id = tb_wordbook.word_book_id " +
            "INNER JOIN tb_word ON tb_wordbook_word.word_id = tb_word.word_id " +
            "WHERE tb_wordbook_word.word_book_id = :wordBookId ORDER BY tb_word.word_str ASC")
    List<CollectWord> getCollectWordsFromWordBookSync(long wordBookId);

}
