package com.tachiyu.lifelog.model.db

import android.content.Context
import androidx.room.*
import com.tachiyu.lifelog.model.db.location.LocationDao
import com.tachiyu.lifelog.model.db.others.OthersDao
import com.tachiyu.lifelog.model.db.sleep.SleepDao
import com.tachiyu.lifelog.Location
import com.tachiyu.lifelog.Others
import com.tachiyu.lifelog.Sleep
import com.tachiyu.lifelog.Transition
import com.tachiyu.lifelog.model.db.transition.TransitionDao

@Database(entities = [
    (Location::class),
    (Transition::class),
    (Sleep::class),
    (Others::class)
                     ],
    version = 1, exportSchema = false)
abstract class MainRoomDatabase: RoomDatabase() {

    abstract fun LocationDao(): LocationDao
    abstract fun TransitionDao(): TransitionDao
    abstract fun SleepDao(): SleepDao
    abstract fun OthersDao(): OthersDao

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
