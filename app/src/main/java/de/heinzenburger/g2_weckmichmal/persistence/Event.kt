package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.heinzenburger.g2_weckmichmal.MainActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val context: Context
): PersistenceClass() {
    @Entity(tableName = "evententity", primaryKeys = ["configID","days"])
    data class EventEntity(
        @ColumnInfo(name = "configID") var configID: Long,
        @ColumnInfo(name = "wakeuptime") var wakeUpTime: LocalTime,
        @ColumnInfo(name = "days") var days: Set<DayOfWeek>,
        @ColumnInfo(name = "date") var date: LocalDate
    ){
        fun log(){
            MainActivity.log.info("Logging Event with id $configID:\n$wakeUpTime\n$days\n$date")
        }
    }

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

    @Database(entities = [EventEntity::class], version = 2)
    @TypeConverters(DateConverter::class)
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
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }

    private var database: AppDatabase = AppDatabase.getDatabase(context)

    override fun saveOrUpdate(event: EventEntity): Boolean {
        try {
            database.configurationDao().deleteByIdAndDays(event.configID,
                DateConverter().fromSetOfDays(event.days).toString()
            )
            database.configurationDao().insert(event)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }

    override fun getAllEvents(): List<EventEntity>? {
        try {
            val result = database.configurationDao().getAll()
            return result
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return null
        }
    }

    override fun removeEvent(configID: Long, days: Set<DayOfWeek>): Boolean {
        try {
            database.configurationDao().deleteByIdAndDays(configID,
                DateConverter().fromSetOfDays(days).toString()
            )
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }
    override fun removeEvent(configID: Long): Boolean {
        try {
            database.configurationDao().deleteById(configID)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }
}
