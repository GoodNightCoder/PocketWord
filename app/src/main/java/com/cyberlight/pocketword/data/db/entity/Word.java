package com.cyberlight.pocketword.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_word",
        indices = {@Index(value = {"word_str"}, unique = true)})
public class Word {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "word_id", typeAffinity = ColumnInfo.INTEGER)
    private long wordId;

    @NonNull
    @ColumnInfo(name = "word_str", typeAffinity = ColumnInfo.TEXT)
    private String wordStr;

    @ColumnInfo(name = "mean", typeAffinity = ColumnInfo.TEXT)
    private String mean;

    @ColumnInfo(name = "accent", typeAffinity = ColumnInfo.TEXT)
    private String accent;

    @ColumnInfo(name = "audio", typeAffinity = ColumnInfo.TEXT)
    private String audio;

    public Word(long wordId, @NonNull String wordStr, String mean, String accent, String audio) {
        this.wordId = wordId;
        this.wordStr = wordStr;
        this.mean = mean;
        this.accent = accent;
        this.audio = audio;
    }

    @Ignore
    public Word(@NonNull String wordStr, String mean, String accent, String audio) {
        this.wordStr = wordStr;
        this.mean = mean;
        this.accent = accent;
        this.audio = audio;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    @NonNull
    public String getWordStr() {
        return wordStr;
    }

    public void setWordStr(@NonNull String wordStr) {
        this.wordStr = wordStr;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
