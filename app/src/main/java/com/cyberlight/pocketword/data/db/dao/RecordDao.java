package com.cyberlight.pocketword.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cyberlight.pocketword.data.db.entity.Record;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Void> insertRecord(Record record);

    @Update
    ListenableFuture<Void> updateRecord(Record record);

    @Delete
    ListenableFuture<Void> deleteRecord(Record record);

    @Query("SELECT * FROM tb_record WHERE date = :date")
    Record getRecordByDateSync(LocalDate date);

    @Query("SELECT * FROM tb_record WHERE date > :date ORDER BY date ASC")
    LiveData<List<Record>> getRecordsAfter(LocalDate date);

    @Query("SELECT * FROM tb_record WHERE study_count >= :studyCount ORDER BY date ASC")
    LiveData<List<Record>> getRecordsByStudyCount(int studyCount);
}