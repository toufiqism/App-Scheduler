package com.sol.appscheduler.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity representing a scheduled app launch
 * @property id Auto-generated primary key
 * @property packageName Package name of the target app
 * @property triggerTime Timestamp for scheduled launch
 * @property isCompleted Whether the schedule has been executed
 * @property created Creation timestamp of the schedule
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val triggerTime: Long,
    val isCompleted: Boolean = false,
    val created: Long = System.currentTimeMillis()
)

/**
 * Data Access Object (DAO) for schedule operations
 */
@Dao
interface ScheduleDao {
    // CRUD operations
    @Insert
    suspend fun insert(schedule: Schedule)
    
    @Update
    suspend fun update(schedule: Schedule)
    
    @Delete
    suspend fun delete(schedule: Schedule)

    // Query to get schedule by ID
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Int): Schedule?

    // Observable Flow of all schedules sorted by time
    @Query("SELECT * FROM schedules ORDER BY triggerTime ASC")
    fun getAll(): Flow<List<Schedule>>
} 