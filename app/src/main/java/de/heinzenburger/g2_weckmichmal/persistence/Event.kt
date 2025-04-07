package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration.AppDatabase
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration.ConfigurationEntity
import java.time.LocalDate
import java.time.LocalTime
import java.util.BitSet

data class Event(
    val context: Context
): Persistence{
    @Entity(tableName = "evententity")
    data class EventEntity(
        @PrimaryKey val configID: Long,
        @ColumnInfo(name = "wakeuptime") var wakeUpTime: String,
        @ColumnInfo(name = "days") var days: String,
        @ColumnInfo(name = "date") var date: String
    ){
        fun log(){
            MainActivity.log.info("Logging Alarm configuration with id $configID:\n$wakeUpTime\n$days\n$date")
        }
    }

    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM evententity")
        fun getAll() : List<EventEntity>

        @Query("SELECT * FROM evententity WHERE configID = :configID")
        fun getById(configID: Long) : List<EventEntity>

        @Query("DELETE FROM evententity WHERE configID = :configID AND days = :days")
        fun deleteByIdAndDays(configID: Long, days: String)

        @Insert
        fun insert(configuration: EventEntity)

        @Delete
        fun delete(configuration: EventEntity)
    }

    @Database(entities = [EventEntity::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun configurationDao(): ConfigurationDao

        companion object{
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context): AppDatabase{
                return INSTANCE?: synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "database"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(config: AlarmConfiguration.ConfigurationEntity): Boolean {
        TODO("Not yet implemented")
    }

    override fun saveOrUpdate(event: EventEntity): Boolean {
        try {
            database.configurationDao().deleteByIdAndDays(event.configID, event.days)
            database.configurationDao().insert(event)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }

    override fun getAlarmConfiguration(id: Long): AlarmConfiguration.ConfigurationEntity {
        TODO("Not yet implemented")
    }

    override fun getAllAlarmConfigurations(): List<AlarmConfiguration.ConfigurationEntity> {
        TODO("Not yet implemented")
    }

    override fun getAllEvents(): List<EventEntity> {
        return  database.configurationDao().getAll()
    }

    override fun removeAlarmConfiguration(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeEvent(configID: Long, days: String): Boolean {
        try {
            database.configurationDao().deleteByIdAndDays(configID, days)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }
}
