package com.cyberlight.pocketword.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cyberlight.pocketword.data.db.entity.Word;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface WordDao {
    // TODO: 测试返回的int是否是word_id
    // 同步操作、返回int是为了紧接着把Word导入词书
    @Insert
    long insertWordSync(Word word);

    @Update
    ListenableFuture<Void> updateWord(Word word);

    @Delete
    ListenableFuture<Void> deleteWord(Word word);


    @Query("SELECT tb_word.* FROM tb_wordbook_word " +
            "INNER JOIN tb_word ON tb_wordbook_word.word_id = tb_word.word_id " +
            "WHERE tb_wordbook_word.word_book_id = :wordBookId")
    List<Word> getWordsFromWordBookSync(long wordBookId);

    @Query("SELECT * FROM tb_word WHERE word_str LIKE :wordStr")
    Word getMatchWordSync(String wordStr);

    // 构建词典时使用该方法
    // fixme：数据库设计完成后去除
    @Insert
    ListenableFuture<Void> insertWords(List<Word> words);
}