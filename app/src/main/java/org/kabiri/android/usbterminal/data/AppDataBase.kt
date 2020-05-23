package org.kabiri.android.usbterminal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.kabiri.android.usbterminal.DATABASE_NAME
import org.kabiri.android.usbterminal.model.WifiDevice


/**
 * Created by Ali Kabiri on 23.05.20.
 */
@Database(entities = [WifiDevice::class], version = 1, exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun wifiDeviceDao(): WifiDeviceDao

    companion object {

        // for singleton instantiation
        @Volatile private var instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDataBase {
            return Room.databaseBuilder(context, AppDataBase::class.java, DATABASE_NAME)
                .build()
        }
    }
}