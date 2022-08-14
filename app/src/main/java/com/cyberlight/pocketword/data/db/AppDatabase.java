package com.cyberlight.pocketword.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.cyberlight.pocketword.data.db.converter.BooleanConverter;
import com.cyberlight.pocketword.data.db.converter.DateConverter;
import com.cyberlight.pocketword.data.db.dao.CollectWordDao;
import com.cyberlight.pocketword.data.db.dao.RecordDao;
import com.cyberlight.pocketword.data.db.dao.WordBookDao;
import com.cyberlight.pocketword.data.db.dao.WordBookWordDao;
import com.cyberlight.pocketword.data.db.dao.WordDao;
import com.cyberlight.pocketword.data.db.entity.Record;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.data.db.entity.WordBookWord;


@Database(
        entities = {Word.class, WordBook.class, WordBookWord.class, Record.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({BooleanConverter.class, DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "pocketword.db";
    private static AppDatabase sInstance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
//                    .createFromAsset("database/prepopulate.db")
                    .build();
        }
        return sInstance;
    }

    public abstract WordDao wordDao();

    public abstract WordBookDao wordBookDao();

    public abstract WordBookWordDao wordBookWordDao();

    public abstract CollectWordDao collectWordDao();

    public abstract RecordDao recordDao();

}