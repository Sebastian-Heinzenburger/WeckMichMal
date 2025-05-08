package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Event
import java.time.DayOfWeek

data class Event(
    val context: Context
): I_Event {
    val logger = Logger(context)
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM evententity")
        fun getAll() : List<EventEntity>

        @Query("SELECT * FROM evententity WHERE configID = :configID AND days = :days")
        fun getByIdAndDays(configID: Long, days: String) : EventEntity

        @Query("DELETE FROM evententity WHERE configID = :configID AND days = :days")
        fun deleteByIdAndDays(configID: Long, days: String)

        @Query("DELETE FROM evententity WHERE configID = :configID")
        fun deleteById(configID: Long)

        @Insert
        fun insert(configuration: EventEntity)

        @Delete
        fun delete(configuration: EventEntity)
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(event: EventEntity): Boolean {
        try {
            database.eventConfigurationDao().deleteByIdAndDays(event.configID,
                DataConverter().fromSetOfDays(event.days).toString()
            )
            database.eventConfigurationDao().insert(event)
            return true
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return false
        }
    }

    override fun getAllEvents(): List<EventEntity>? {
        try {
            val result = database.eventConfigurationDao().getAll()
            return result
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return null
        }
    }

    override fun removeEvent(configID: Long, days: Set<DayOfWeek>): Boolean {
        try {
            database.eventConfigurationDao().deleteByIdAndDays(configID,
                DataConverter().fromSetOfDays(days).toString()
            )
            return true
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return false
        }
    }
    override fun removeEvent(configID: Long): Boolean {
        try {
            database.eventConfigurationDao().deleteById(configID)
            return true
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return false
        }
    }

    override fun getEvent(id: Long, days: Set<DayOfWeek>): EventEntity? {
        try {
            return database.eventConfigurationDao().getByIdAndDays(id, DataConverter().fromSetOfDays(days).toString())
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return null
        }
    }
}