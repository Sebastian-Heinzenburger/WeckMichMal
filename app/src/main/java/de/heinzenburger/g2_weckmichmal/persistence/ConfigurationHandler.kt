package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationHandlerSpecification
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import java.time.LocalDate

data class ConfigurationHandler(
    val context: Context,
): ConfigurationHandlerSpecification {
    val logger = Logger(context)
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM configuration")
        fun getAll() : List<Configuration>

        @Query("SELECT * FROM configuration WHERE uid = :uid")
        fun getById(uid: Long) : Configuration

        @Query("UPDATE configuration SET isActive = :isActive WHERE uid = :uid")
        fun updateActiveById(uid: Long, isActive: Boolean)

        @Query("UPDATE configuration SET ichHabGeringt = :date WHERE uid = :uid")
        fun updateIchHabGeringtById(uid: Long, date: LocalDate)

        @Query("DELETE FROM configuration WHERE uid = :uid")
        fun deleteById(uid: Long)

        @Insert
        fun insert(configuration: Configuration)

        @Delete
        fun delete(configuration: Configuration)

        //Combining Events with their corresponding Configuration
        @Transaction
        @Query("SELECT * FROM configuration")
        fun getConfigurationsAndEvents(): List<ConfigurationWithEvent>?
        @Transaction
        @Query("SELECT * FROM configuration WHERE uid = :uid")
        fun getConfigurationAndEvent(uid: Long): ConfigurationWithEvent
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(config: Configuration) {
        try {
            //Updating means deleting and inserting again
            database.alarmConfigurationDao().deleteById(config.uid)
            database.alarmConfigurationDao().insert(config)
        }
        catch (e: Exception){
            throw PersistenceException.UpdateConfigurationException(e)
        }
    }

    override fun updateConfigurationActive(isActive: Boolean, uid: Long) {
        try {
            //Updating means deleting and inserting again
            database.alarmConfigurationDao().updateActiveById(uid, isActive)
        }
        catch (e: Exception){
            throw PersistenceException.UpdateConfigurationException(e)
        }
    }

    override fun updateConfigurationIchHabGeringt(date: LocalDate, uid: Long) {
        try {
            //Updating means deleting and inserting again
            database.alarmConfigurationDao().updateIchHabGeringtById(uid, date)
        }
        catch (e: Exception){
            throw PersistenceException.UpdateConfigurationException(e)
        }
    }

    override fun getAlarmConfiguration(id: Long): Configuration? {
        try {
            val result = database.alarmConfigurationDao().getById(id)
            return result
        }
        catch (e: Exception){
            throw PersistenceException.GetConfigurationException(e)
        }
    }

    override fun getAllAlarmConfigurations(): List<Configuration>? {
        try {
            val result = database.alarmConfigurationDao().getAll()
            return result
        }
        catch (e: Exception){
            throw PersistenceException.GetConfigurationException(e)
        }
    }

    override fun removeAlarmConfiguration(id: Long){
        try {
            database.alarmConfigurationDao().deleteById(id)
        }
        catch (e: Exception){
            throw PersistenceException.UpdateConfigurationException(e)
        }
    }

    override fun getConfigurationAndEvent(id: Long): ConfigurationWithEvent? {
        try {
            val result = database.alarmConfigurationDao().getConfigurationAndEvent(id)
            return result
        }
        catch (e: Exception){
            throw PersistenceException.GetConfigurationException(e)
        }
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        try {
            val result = database.alarmConfigurationDao().getConfigurationsAndEvents()
            return result
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE,e.message.toString())
            throw PersistenceException.GetConfigurationException(e)
        }
    }
}
