package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.InterfaceConfigurationHandler

data class ConfigurationHandler(
    val context: Context,
): InterfaceConfigurationHandler {
    val logger = Logger(context)
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM configuration")
        fun getAll() : List<Configuration>

        @Query("SELECT * FROM configuration WHERE uid = :uid")
        fun getById(uid: Long) : Configuration

        @Query("UPDATE configuration SET isActive = :isActive WHERE uid = :uid")
        fun updateActiveById(uid: Long, isActive: Boolean)

        @Query("DELETE FROM configuration WHERE uid = :uid")
        fun deleteById(uid: Long)

        @Insert
        fun insert(configuration: Configuration)

        @Delete
        fun delete(configuration: Configuration)

        //Combining Events with their corresponding Configuration
        @Transaction
        @Query("SELECT * FROM configuration")
        fun getConfigurationsAndEvents(): List<ConfigurationWithEvent>
        @Transaction
        @Query("SELECT * FROM configuration WHERE uid = :uid")
        fun getConfigurationAndEvent(uid: Long): ConfigurationWithEvent
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(config: Configuration): Boolean {
        try {
            //Updating means deleting and inserting again
            database.alarmConfigurationDao().deleteById(config.uid)
            database.alarmConfigurationDao().insert(config)
            return true
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return false
        }
    }

    override fun updateConfigurationActive(isActive: Boolean, uid: Long): Boolean {
        try {
            //Updating means deleting and inserting again
            database.alarmConfigurationDao().updateActiveById(uid, isActive)
            return true
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return false
        }
    }

    override fun getAlarmConfiguration(id: Long): Configuration? {
        try {
            val result = database.alarmConfigurationDao().getById(id)
            return result
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return null
        }
    }

    override fun getAllAlarmConfigurations(): List<Configuration>? {
        try {
            val result = database.alarmConfigurationDao().getAll()
            return result
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return null
        }
    }

    override fun removeAlarmConfiguration(id: Long): Boolean {
        try {
            database.alarmConfigurationDao().deleteById(id)
            return true
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return false
        }
    }

    override fun getConfigurationAndEvent(id: Long): ConfigurationWithEvent? {
        try {
            val result = database.alarmConfigurationDao().getConfigurationAndEvent(id)
            return result
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return null
        }
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        try {
            val result = database.alarmConfigurationDao().getConfigurationsAndEvents()
            return result
        }
        catch (e: Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            e.printStackTrace()
            return null
        }
    }
}
