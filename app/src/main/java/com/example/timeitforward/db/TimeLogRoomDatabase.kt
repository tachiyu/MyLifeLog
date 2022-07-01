package com.example.timeitforward

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import com.example.timeitforward.db.TimeLog
import com.example.timeitforward.db.TimeLogDao
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@TypeConverters(DateTimeConverter::class)
@Database(entities = [(TimeLog::class)], version = 1)
abstract class TimeLogRoomDatabase: RoomDatabase() {

    abstract fun TimeLogDao(): TimeLogDao

    companion object {

        private  var INSTANCE: TimeLogRoomDatabase? = null

        fun getInstance(context: Context): TimeLogRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TimeLogRoomDatabase::class.java,
                        "time_log_database"
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