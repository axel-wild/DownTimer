package de.wildsolutions.downtimer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.lang.System.*

/**
 * Data Access Object (DAO) for stored time measurements.
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */
@Dao
interface TimingDao {

    @Query("SELECT * FROM timing WHERE id = (SELECT MAX(id) FROM timing)")
    fun getLast(): Timing

    @Query("SELECT * FROM timing ORDER BY start DESC")
    fun getAll(): Array<Timing>

    @Query("SELECT COUNT(*) FROM timing")
    fun count(): Int

    @Insert
    fun insert(timing: Timing)

    @Update
    fun update(timing: Timing)

    @Query("DELETE FROM timing")
    fun truncate(): Int

    @Query("select SUM(stop - start) as sum FROM timing where stop > 0")
    fun sum(): Long

}