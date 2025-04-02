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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.BitSet
import java.util.logging.Logger

class AlarmConfiguration: Persistence {
    @Entity(tableName = "configurationentity")
    data class ConfigurationEntity(
        @PrimaryKey(autoGenerate = true) val uid: Long = 0,
        @ColumnInfo(name = "name") val name : String,
        @ColumnInfo(name = "day") val day: Int,
        @ColumnInfo(name = "fixedArrivalTime") val fixedArrivalTime: String,
        @ColumnInfo(name = "fixedTravelBuffer") val fixedTravelBuffer: Int,
        @ColumnInfo(name = "startBuffer") val startBuffer: Int,
        @ColumnInfo(name = "endBuffer") val endBuffer: Int,
        @ColumnInfo(name = "startStation") val startStation: String,
        @ColumnInfo(name = "endStation") val endStation: String
    )
    @Dao
    interface ConfigurationDao{
        @Query("Select * FROM configurationentity")
        fun getAll() : List<ConfigurationEntity>

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


    override fun saveOrUpdate(config: AlarmConfiguration) {
        TODO("Not yet implemented")
    }

    override fun saveOrUpdate(event: Event) {
        TODO("Not yet implemented")
    }

    override fun getAlarmConfiguration(id: String): AlarmConfiguration {
        TODO("Not yet implemented")
    }

    override fun getAllAlarmConfigurations(): List<AlarmConfiguration> {
        TODO("Not yet implemented")
    }

    override fun getAllEvents(): List<Event> {
        TODO("Not yet implemented")
    }

    override fun removeAlarmConfiguration(id: String) {
        TODO("Not yet implemented")
    }

    override fun removeEvent(configID: String, day: BitSet) {
        TODO("Not yet implemented")
    }

    companion object{
        fun initDatabase(context: Context) {
            val log = Logger.getLogger(AlarmConfiguration::class.java.name)
            val bitSet = BitSet()
            bitSet.set(0,6,true)
            val fixedArrivalTime = LocalTime.parse("20:00:00")
            val testConfiguration = ConfigurationEntity(
                name = "peullo",
                uid = 0,
                day = 159,
                fixedArrivalTime = fixedArrivalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                fixedTravelBuffer = 20,
                startBuffer = 30,
                endBuffer = 0,
                startStation = "Durlach",
                endStation = "Exmatrikulation"
            )

            val database = AppDatabase.getDatabase(context)
            database.configurationDao().insert(testConfiguration)
            val configurations: List<ConfigurationEntity> = database.configurationDao().getAll()
            for (configuration in configurations){
                log.warning(configuration.name)
            }
        }
    }
}
