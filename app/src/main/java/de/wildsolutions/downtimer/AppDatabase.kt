package de.wildsolutions.downtimer

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */

@Database(entities = [Timing::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Returns the DAO for this application.
     * @return The DAO for this application.
     */
    abstract fun timingDao(): TimingDao

    companion object {

        private const val TAG = "AppDatabase"

        const val VERSION = 1
        private const val DATABASE_NAME = "inventory_database.db"

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Gets and returns the database instance if exists; otherwise, builds a new database.
         * @param context The context to access the application context.
         * @return The database instance.
         */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }


        /**
         * Builds and returns the database.
         * @param appContext The application context to reference.
         * @return The built database.
         */
        private fun buildDatabase(appContext: Context): AppDatabase {
            val builder = Room.databaseBuilder(
                appContext,
                AppDatabase::class.java, "downtimer.sqlite"
            )

            return builder.build()
        }
    }
}