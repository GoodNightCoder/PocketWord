package com.cyberlight.pocketword.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cyberlight.pocketword.data.db.entity.Settings;
import com.google.common.util.concurrent.ListenableFuture;

@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Void> insertSettings(Settings settings);

    @Query("SELECT * FROM tb_settings")
    LiveData<Settings> getSettingsLiveData();

    @Query("SELECT * FROM tb_settings")
    Settings getSettingsSync();

}