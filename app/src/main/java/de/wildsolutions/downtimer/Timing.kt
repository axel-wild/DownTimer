package de.wildsolutions.downtimer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
}
