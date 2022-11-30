package com.example.timeitforward.model.db

import android.content.Context
import androidx.room.*
import com.example.timeitforward.model.db.timelog.TimeLog
import com.example.timeitforward.model.db.timelog.TimeLogDao
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.db.transition.TransitionDao
import java.time.LocalDateTime


@TypeConverters(DateTimeConverter::class)
@Database(entities = [(TimeLog::class), (Transition::class)], version = 1, exportSchema = false)
abstract class MainRoomDatabase: RoomDatabase() {

    abstract fun TimeLogDao(): TimeLogDao
    abstract fun TransitionDao(): TransitionDao

    companion object {

        private  var INSTANCE: MainRoomDatabase? = null

        fun getInstance(context: Context): MainRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MainRoomDatabase::class.java,
                        "my_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

class DateTimeConverter {
    
    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return if (value == null) null else LocalDateTime.parse(value)
    }

    @TypeConverter
    fun toLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}