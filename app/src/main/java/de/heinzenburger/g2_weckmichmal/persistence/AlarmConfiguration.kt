package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationAndEventEntity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_AlarmConfiguration

data class AlarmConfiguration(
    val context: Context,
): I_AlarmConfiguration {
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM configurationentity")
        fun getAll() : List<ConfigurationEntity>

        @Query("SELECT * FROM configurationentity WHERE uid = :uid")
        fun getById(uid: Long) : ConfigurationEntity

        @Query("UPDATE configurationentity SET isActive = :isActive WHERE uid = :uid")
        fun updateActiveById(uid: Long, isActive: Boolean)

        @Query("DELETE FROM configurationentity WHERE uid = :uid")
        fun deleteById(uid: Long)

        @Insert
        fun insert(configuration: ConfigurationEntity)

        @Delete
        fun delete(configuration: ConfigurationEntity)

        //Combining Events with their corresponding Configuration
        @Transaction
        @Query("SELECT * FROM configurationentity")
        fun getConfigurationsAndEvents(): List<ConfigurationAndEventEntity>
        @Transaction
        @Query("SELECT * FROM configurationentity WHERE uid = :uid")
        fun getConfigurationAndEvent(uid: Long): ConfigurationAndEventEntity
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(config: ConfigurationEntity): Boolean {
        try {
            //Updating means deleting and inserting again
            database.alarmConfigurationDao().deleteById(config.uid)
            database.alarmConfigurationDao().insert(config)
            return true
        }
        catch (e: Exception){
            MainActivity.log.severe(e.message)
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
            MainActivity.log.severe(e.message)
            e.printStackTrace()
            return false
        }
    }

    override fun getAlarmConfiguration(id: Long): ConfigurationEntity? {
        try {
            val result = database.alarmConfigurationDao().getById(id)
            return result
        }
        catch (e: Exception){
            MainActivity.log.severe(e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun getAllAlarmConfigurations(): List<ConfigurationEntity>? {
        try {
            val result = database.alarmConfigurationDao().getAll()
            return result
        }
        catch (e: Exception){
            MainActivity.log.severe(e.message)
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
            MainActivity.log.severe(e.message)
            e.printStackTrace()
            return false
        }
    }

    override fun getConfigurationAndEvent(id: Long): ConfigurationAndEventEntity? {
        try {
            val result = database.alarmConfigurationDao().getConfigurationAndEvent(id)
            return result
        }
        catch (e: Exception){
            MainActivity.log.severe(e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationAndEventEntity>? {
        try {
            val result = database.alarmConfigurationDao().getConfigurationsAndEvents()
            return result
        }
        catch (e: Exception){
            MainActivity.log.severe(e.message)
            e.printStackTrace()
            return null
        }
    }
}
