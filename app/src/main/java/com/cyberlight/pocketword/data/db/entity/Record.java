package com.cyberlight.pocketword.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "tb_record")
public class Record {

    @PrimaryKey
    @ColumnInfo(name = "date", typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private LocalDate date;

    @ColumnInfo(name = "study_count", typeAffinity = ColumnInfo.INTEGER)
    private int studyCount;

    @ColumnInfo(name = "study_duration", typeAffinity = ColumnInfo.INTEGER)
    private long studyDuration;

    public Record(@NonNull LocalDate date, int studyCount, long studyDuration) {
        this.date = date;
        this.studyCount = studyCount;
        this.studyDuration = studyDuration;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    public int getStudyCount() {
        return studyCount;
    }

    public void setStudyCount(int studyCount) {
        this.studyCount = studyCount;
    }

    public long getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(long studyDuration) {
        this.studyDuration = studyDuration;
    }
}