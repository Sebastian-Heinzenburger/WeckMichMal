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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.BitSet
import java.util.logging.Logger

data class AlarmConfiguration(
    val context: Context,
): Persistence {
    @Entity(tableName = "configurationentity")
    data class ConfigurationEntity(
        @PrimaryKey val uid: Long = System.currentTimeMillis(),
        @ColumnInfo(name = "name") var name : String,
        @ColumnInfo(name = "days") var days: String,
        @ColumnInfo(name = "fixedArrivalTime") var fixedArrivalTime: String,
        @ColumnInfo(name = "fixedTravelBuffer") var fixedTravelBuffer: Int,
        @ColumnInfo(name = "startBuffer") var startBuffer: Int,
        @ColumnInfo(name = "endBuffer") var endBuffer: Int,
        @ColumnInfo(name = "startStation") var startStation: String,
        @ColumnInfo(name = "endStation") var endStation: String
    ){
        fun log(){
            MainActivity.log.info("Logging Alarm configuration with id $uid:\n$name\n$days\n$fixedArrivalTime\n$fixedTravelBuffer\n$startBuffer\n$endBuffer\n$startStation\n$endStation")
        }
    }
    @Dao
    interface ConfigurationDao{
        @Query("SELECT * FROM configurationentity")
        fun getAll() : List<ConfigurationEntity>

        @Query("SELECT * FROM configurationentity WHERE uid = :uid")
        fun getById(uid: Long) : List<ConfigurationEntity>

        @Query("DELETE FROM configurationentity WHERE uid = :uid")
        fun deleteById(uid: Long)

        @Insert
        fun insert(configuration: ConfigurationEntity)

        @Delete
        fun delete(configuration: ConfigurationEntity)
    }

    @Database(entities = [ConfigurationEntity::class], version = 1)
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

    override fun saveOrUpdate(config: ConfigurationEntity): Boolean {
        try {
            database.configurationDao().deleteById(config.uid)
            database.configurationDao().insert(config)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }

    }

    override fun saveOrUpdate(event: Event): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAlarmConfiguration(id: Long): ConfigurationEntity {
        return database.configurationDao().getById(id)[0]
    }

    override fun getAllAlarmConfigurations(): List<ConfigurationEntity> {
        return database.configurationDao().getAll()
    }

    override fun getAllEvents(): List<Event> {
        TODO("Not yet implemented")
    }

    override fun removeAlarmConfiguration(id: Long): Boolean {
        try {
            database.configurationDao().deleteById(id)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }

    override fun removeEvent(configID: String, day: BitSet): Boolean {
        TODO("Not yet implemented")
    }
}
