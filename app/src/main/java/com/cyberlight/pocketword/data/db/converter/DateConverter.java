package com.cyberlight.pocketword.data.db.converter;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public class DateConverter {
    @TypeConverter
    public LocalDate toLocalDate(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    @TypeConverter
    public long toLong(LocalDate date) {
        return date.toEpochDay();
    }
}
