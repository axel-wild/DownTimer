package de.wildsolutions.downtimer

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val CREDIT_HOURS_KEY = "credit_hours"
private const val FREE_DAYS_KEY = "free_days"
private const val DATE_START_KEY = "date_start"
private const val DATE_END_KEY = "date_end"


/**
 * Class that handles saving and retrieving user preferences
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
*/
class DataStoreManager private constructor(context: Context) {

    private val sharedPreferences =
        context.applicationContext.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getCreditHours() : Float{
        return sharedPreferences.getFloat(CREDIT_HOURS_KEY, 0f)
    }

    suspend fun setCreditHours(creditHours : Float) {
        sharedPreferences.edit {
            putFloat(CREDIT_HOURS_KEY, creditHours)
        }
    }

    fun getFreeDays() : Int{
        return sharedPreferences.getInt(FREE_DAYS_KEY, 0)
    }

    suspend fun setFreeDays(freeDays : Int) {
        sharedPreferences.edit {
            putInt(FREE_DAYS_KEY, freeDays)
        }
    }

    fun getDateStart() : Long {
        return sharedPreferences.getLong(DATE_START_KEY, LocalDate.now().toEpochDay())
    }

    suspend fun setDateStart(dateStart : Long) {
        sharedPreferences.edit {
            putLong(DATE_START_KEY, dateStart)
        }
    }

    fun getDateEnd() : Long {
        return sharedPreferences.getLong(DATE_END_KEY, LocalDate.now().toEpochDay())
    }

    suspend fun setDateEnd(dateEnd : Long) {
        sharedPreferences.edit {
            putLong(DATE_END_KEY, dateEnd)
        }
    }

    fun getDaysDiff() : Long {
        val dateStart = LocalDate.ofEpochDay(getDateStart())
        val dateEnd = LocalDate.ofEpochDay(getDateEnd())
        return dateStart.until(dateEnd, ChronoUnit.DAYS)
    }

    fun getWorkingDays() : Double {
        val calWeeks : Double = getDaysDiff() / 7.0
        val daysFree = getFreeDays()
        return (calWeeks * 5 - daysFree)
    }

    fun getTotalTimeToWorkMillis() : Long {
        val workingDays = getWorkingDays()
        //     week         * Anrechnungsstunden mit 100 Minutenregel in milliseconds
        val millisF = workingDays * getWeeklyWorkingMillis() / 5.0
        return millisF.toLong()
    }

    fun getWeeklyWorkingMillis() : Long {
        return (getCreditHours() * 100 * 60000).toLong()
    }

    companion object {
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = DataStoreManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}