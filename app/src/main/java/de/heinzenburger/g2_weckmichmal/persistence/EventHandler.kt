package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.InterfaceEventHandler
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import java.time.DayOfWeek

data class EventHandler(
    val context: Context
): InterfaceEventHandler {
    val logger = Logger(context)
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM event")
        fun getAll() : List<Event>

        @Query("SELECT * FROM event WHERE configID = :configID AND days = :days")
        fun getByIdAndDays(configID: Long, days: String) : Event

        @Query("DELETE FROM event WHERE configID = :configID AND days = :days")
        fun deleteByIdAndDays(configID: Long, days: String)

        @Query("DELETE FROM event WHERE configID = :configID")
        fun deleteById(configID: Long)

        @Insert
        fun insert(configuration: Event)

        @Delete
        fun delete(configuration: Event)
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(event: Event) {
        try {
            database.eventConfigurationDao().deleteById(event.configID)
            database.eventConfigurationDao().insert(event)
        }
        catch (e: Exception){
            throw PersistenceException.UpdateEventException(e)
        }
    }

    override fun getAllEvents(): List<Event>? {
        try {
            val result = database.eventConfigurationDao().getAll()
            return result
        }
        catch (e: Exception){
            throw PersistenceException.GetEventException(e)
        }
    }

    override fun removeEvent(configID: Long) {
        try {
            database.eventConfigurationDao().deleteById(configID)
        }
        catch (e: Exception){
            throw PersistenceException.UpdateEventException(e)
        }
    }

    override fun getEvent(id: Long, days: Set<DayOfWeek>): Event? {
        try {
            return database.eventConfigurationDao().getByIdAndDays(id, DataConverter().fromSetOfDays(days).toString())
        }
        catch (e: Exception){
            throw PersistenceException.GetEventException(e)
        }
    }
}