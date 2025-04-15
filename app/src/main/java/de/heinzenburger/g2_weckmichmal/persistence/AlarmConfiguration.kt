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
import androidx.room.TypeConverters
import de.heinzenburger.g2_weckmichmal.MainActivity
import java.time.DayOfWeek
import java.time.LocalTime

data class AlarmConfiguration(
    val context: Context,
): PersistenceClass() {
    @Entity(tableName = "configurationentity")
    data class ConfigurationEntity(
        @PrimaryKey val uid: Long = System.currentTimeMillis(),
        @ColumnInfo(name = "name") var name : String,
        @ColumnInfo(name = "days") var days: Set<DayOfWeek>,
        @ColumnInfo(name = "fixedArrivalTime") var fixedArrivalTime: LocalTime?,
        @ColumnInfo(name = "fixedTravelBuffer") var fixedTravelBuffer: Int?,
        @ColumnInfo(name = "startBuffer") var startBuffer: Int,
        @ColumnInfo(name = "endBuffer") var endBuffer: Int,
        @ColumnInfo(name = "startStation") var startStation: String?,
        @ColumnInfo(name = "endStation") var endStation: String?
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
        fun getById(uid: Long) : ConfigurationEntity

        @Query("DELETE FROM configurationentity WHERE uid = :uid")
        fun deleteById(uid: Long)

        @Insert
        fun insert(configuration: ConfigurationEntity)

        @Delete
        fun delete(configuration: ConfigurationEntity)
    }

    @Database(entities = [ConfigurationEntity::class], version = 1)
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

    override fun getAlarmConfiguration(id: Long): ConfigurationEntity? {
        try {
            val result = database.configurationDao().getById(id)
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
            val result = database.configurationDao().getAll()
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
            database.configurationDao().deleteById(id)
            return true
        }
        catch (e: Exception){
            MainActivity.log.warning(e.message)
            e.printStackTrace()
            return false
        }
    }
}
