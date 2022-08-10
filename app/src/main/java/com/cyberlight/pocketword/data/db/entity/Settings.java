package com.cyberlight.pocketword.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tb_settings",
        foreignKeys = {
                @ForeignKey(
                        entity = WordBook.class,
                        parentColumns = "word_book_id",
                        childColumns = "word_book_id",
                        onDelete = ForeignKey.SET_NULL,
                        onUpdate = ForeignKey.SET_NULL
                )
        }
)
public class Settings {
    @PrimaryKey
    @ColumnInfo(name = "settings_id", typeAffinity = ColumnInfo.INTEGER)
    private long settingsId;

    @ColumnInfo(name = "word_book_id", typeAffinity = ColumnInfo.INTEGER)
    private long wordBookId;

    public Settings(long settingsId, long wordBookId) {
        this.settingsId = settingsId;
        this.wordBookId = wordBookId;
    }

    public long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(long settingsId) {
        this.settingsId = settingsId;
    }

    public long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(long wordBookId) {
        this.wordBookId = wordBookId;
    }
}
