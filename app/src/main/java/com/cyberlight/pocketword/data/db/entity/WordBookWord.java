package com.cyberlight.pocketword.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

@Entity(
        tableName = "tb_wordbook_word",
        primaryKeys = {"word_id", "word_book_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = Word.class,
                        parentColumns = "word_id",
                        childColumns = "word_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = WordBook.class,
                        parentColumns = "word_book_id",
                        childColumns = "word_book_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        }
)
public class WordBookWord {
    @ColumnInfo(name = "word_id", typeAffinity = ColumnInfo.INTEGER)
    private long wordId;

    @ColumnInfo(name = "word_book_id", typeAffinity = ColumnInfo.INTEGER)
    private long wordBookId;

    @ColumnInfo(name = "import_at", typeAffinity = ColumnInfo.INTEGER)
    private long importAt;

    @ColumnInfo(name = "known", typeAffinity = ColumnInfo.INTEGER, defaultValue = "0")
    private boolean known;

    public WordBookWord(long wordId, long wordBookId, long importAt, boolean known) {
        this.wordId = wordId;
        this.wordBookId = wordBookId;
        this.importAt = importAt;
        this.known = known;
    }

    @Ignore
    public WordBookWord(long wordId, long wordBookId, long importAt) {
        this.wordId = wordId;
        this.wordBookId = wordBookId;
        this.importAt = importAt;
    }

    // 删除时用
    @Ignore
    public WordBookWord(long wordId, long wordBookId) {
        this.wordId = wordId;
        this.wordBookId = wordBookId;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(long wordBookId) {
        this.wordBookId = wordBookId;
    }

    public long getImportAt() {
        return importAt;
    }

    public void setImportAt(long importAt) {
        this.importAt = importAt;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }
}