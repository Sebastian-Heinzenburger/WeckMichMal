package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import java.time.DayOfWeek

data class AlarmConfiguration(
    val context: Context,
): PersistenceClass() {
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM configurationentity")
        fun getAll() : List<ConfigurationEntity>

        @Query("SELECT * FROM configurationentity WHERE uid = :uid")
        fun getById(uid: Long) : ConfigurationEntity

        @Query("DELETE FROM configurationentity WHERE uid = :uid")
        fun deleteById(uid: Long)

        @Insert
        fun insert(configuration: ConfigurationEntity)

        @Delete
        fun delete(configuration: ConfigurationEntity)
    }



    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(config: ConfigurationEntity): Boolean {
        try {
            database.alarmConfigurationDao().deleteById(config.uid)
            database.alarmConfigurationDao().insert(config)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
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
            MainActivity.log.warning(e.message)
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
            MainActivity.log.warning(e.message)
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
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }
}
