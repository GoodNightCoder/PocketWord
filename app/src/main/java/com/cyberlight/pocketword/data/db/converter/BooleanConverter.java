package com.cyberlight.pocketword.data.db.converter;

import androidx.room.TypeConverter;

public class BooleanConverter {
    @TypeConverter
    public static int toInt(boolean b) {
        return b ? 1 : 0;
    }

    @TypeConverter
    public static boolean toBoolean(int i) {
        return i != 0;
    }
}
