package com.example.timeitforward

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@TypeConverters(DateTimeConverter::class)
@Database(entities = [(TimeRecord::class)], version = 1)
abstract class TimeRecordRoomDatabase: RoomDatabase() {

    abstract fun timeRecordDao(): TimeRecordDao

    companion object {

        private  var INSTANCE: TimeRecordRoomDatabase? = null

        fun getInstance(context: Context): TimeRecordRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TimeRecordRoomDatabase::class.java,
                        "time_record_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

class DateTimeConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return if (value == null) null else LocalDateTime.parse(value)
    }

    @TypeConverter
    fun toLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}