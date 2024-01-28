package de.wildsolutions.downtimer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneId

/**
 * Time measurement object.
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */
@Entity
data class Timing(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "start") var start: Long,
    @ColumnInfo(name = "stop") var stop: Long?,
): Serializable {
    constructor(
    ) : this(null, 0, 0)

    /**
     * Set stop at the end of the day when recording has started
     */
    fun applyMaxRecordingDateTime(){
        val startDate : LocalDate = Instant.ofEpochMilli(this.start).atZone(ZoneId.systemDefault()).toLocalDate()
        this.stop = LocalDateTime.of(startDate.year, startDate.month, startDate.dayOfMonth, 23, 59).toEpochSecond(
            OffsetDateTime.now().getOffset()) * 1000
    }

    /**
     * start recording
     */
    fun start(){
        this.start = System.currentTimeMillis()
    }

    fun isRunning() : Boolean{
        return this.stop == null || this.stop == 0L
    }

    /**
     * stop recording
     */
    fun stop(){
        if(!this.startedToday()){
            this.applyMaxRecordingDateTime()
        }
        else{
            this.stop = System.currentTimeMillis()
        }
    }

    /**
     * checks whether the measurement has started today
     */
    fun startedToday() : Boolean {
        val startDate : LocalDate = Instant.ofEpochMilli(this.start).atZone(ZoneId.systemDefault()).toLocalDate()
        return Period.between(LocalDate.now(), startDate).isZero
    }
}
