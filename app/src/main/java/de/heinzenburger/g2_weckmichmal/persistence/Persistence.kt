package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_PersistenceSpecification
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


abstract class PersistenceClass : I_PersistenceSpecification{
    override fun saveOrUpdate(config: ConfigurationEntity): Boolean {
        return false
    }
    override fun saveOrUpdate(event: EventEntity): Boolean{
        return false
    }
    override fun getAlarmConfiguration(id: Long): ConfigurationEntity? {
        return null
    }
    override fun getAllAlarmConfigurations(): List<ConfigurationEntity>?{
        return null
    }
    override fun getAllEvents(): List<EventEntity>?{
        return null
    }
    override fun removeAlarmConfiguration(id: Long): Boolean{
        return false
    }
    override fun removeEvent(configID: Long, days: Set<DayOfWeek>): Boolean{
        return false
    }
    override fun removeEvent(configID: Long): Boolean{
        return false
    }
    override fun getApplicationSettings(): SettingsEntity? {
        return null
    }
    override fun saveOrUpdateApplicationSettings(settings: SettingsEntity): Boolean {
        return false
    }
    override fun isApplicationOpenedFirstTime(): Boolean? {
        return null
    }
    override fun getEvent(id: Long, days: Set<DayOfWeek>): EventEntity? {
        return null
    }
}

public class DateConverter {
    @TypeConverter
    fun toLocalDate(dateLong: Long?): LocalDate? {
        return if (dateLong == null) {
            null
        } else {
            LocalDate.ofEpochDay(dateLong)
        }
    }
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalTime(timeLong: Long?): LocalTime? {
        return if (timeLong == null) {
            null
        } else {
            LocalTime.ofNanoOfDay(timeLong)
        }
    }
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Long? {
        return time?.toNanoOfDay()
    }

    @TypeConverter
    fun toSetOfDays(stringDays: String?): Set<DayOfWeek>? {
        if (stringDays == null) {
            return null
        } else {
            var result = mutableSetOf<DayOfWeek>()
            for(day in DayOfWeek.entries){
                if(stringDays[day.value-1] == '1'){
                    result.add(day)
                }
            }
            return result
        }
    }
    @TypeConverter
    fun fromSetOfDays(days: Set<DayOfWeek>?): String? {
        var result = ""
        for(day in DayOfWeek.entries){
            result += if(days?.contains(day) == true){
                "1"
            } else{
                "0"
            }
        }
        return result
    }
}


@Database(entities = [ConfigurationEntity::class, EventEntity::class], version = 15,exportSchema = true)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmConfigurationDao(): AlarmConfiguration.ConfigurationDao
    abstract fun eventConfigurationDao(): Event.ConfigurationDao

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
        fun logDatabaseSchema(db: RoomDatabase) {
            val tableCursor = db.query("SELECT name FROM sqlite_master WHERE type='table'", null)
            while (tableCursor.moveToNext()) {
                val tableName = tableCursor.getString(0)
                MainActivity.log.info( "Table: $tableName")

                val columnCursor = db.query("PRAGMA table_info($tableName)", null)
                while (columnCursor.moveToNext()) {
                    val columnName = columnCursor.getString(columnCursor.getColumnIndexOrThrow("name"))
                    val columnType = columnCursor.getString(columnCursor.getColumnIndexOrThrow("type"))
                    MainActivity.log.info("-- Column: $columnName ($columnType)")
                }
                columnCursor.close()
            }
            tableCursor.close()
        }
    }
}
