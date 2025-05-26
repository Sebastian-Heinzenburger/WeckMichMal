package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.Route
import de.heinzenburger.g2_weckmichmal.specifications.RouteSection
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


//Data Converter achieves storing complex data types like dates in a SQLite Database by converting them into Numbers or Strings
class DataConverter {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }
    //LocalDate is converted to days since 1970
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

    //LocalTime is converted to nanoseconds since 00:00 at night
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

    //Set<DayOfWeek> is converted to a 7 char long string where every digit is either 1 or 0
    //E.g. 1000100 for {Monday, Friday}
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

    //All attributes of courses are converted into JSON format
    @TypeConverter
    fun fromListOfCourses(courses: List<Course>?): String?{
        if(courses != null){
            var result = JSONArray()
            courses.forEach {
                var course = JSONObject()
                course.put("name",it.name)
                course.put("lecturer",it.lecturer)
                course.put("room",it.room)
                course.put("startDate",it.startDate)
                course.put("endDate",it.endDate)
                result.put(course)
            }
            return result.toString()
        }
        else {
            return null
        }
    }
    @TypeConverter
    fun toListOfCourses(courses: String?): List<Course>?{
        if(courses != null){
            var result = mutableListOf<Course>()
            var array = JSONArray(courses)
            for (i in 0 until array.length()) {
                val jsonObject = array.getJSONObject(i)
                result.add(Course(
                    name = jsonObject.optString("name"),
                    lecturer = jsonObject.optString("lecturer"),
                    room = jsonObject.optString("room"),
                    startDate = LocalDateTime.parse(jsonObject.getString("startDate"), formatter),
                    endDate = LocalDateTime.parse(jsonObject.getString("endDate"), formatter),
                ))
            }
            return result
        }
        return null
    }

    //All attributes of routes are converted into JSON format
    @TypeConverter
    fun fromListOfRoutes(routes: List<Route>?): String?{
        if(routes != null){
            var result = JSONArray()
            routes.forEach {
                var route = JSONObject()
                route.put("startStation",it.startStation)
                route.put("endStation",it.endStation)
                route.put("startTime",it.startTime)
                route.put("endTime",it.endTime)
                var sections = JSONArray()
                it.sections.forEach {
                    var section = JSONObject()
                    section.put("vehicleName",it.vehicleName)
                    section.put("startTime",it.startTime)
                    section.put("endTime",it.endTime)
                    section.put("startStation",it.startStation)
                    section.put("endStation",it.endStation)
                    sections.put(section)
                }
                route.put("sections",sections)
                result.put(route)
            }
            return result.toString()
        }
        else{
            return null
        }

    }
    @TypeConverter
    fun toListOfRoutes(routes: String?): List<Route>?{
        if(routes != null){
            var result = mutableListOf<Route>()
            var array = JSONArray(routes)
            for (i in 0 until array.length()) {
                val jsonObject = array.getJSONObject(i)
                val sections = mutableListOf<RouteSection>()
                val sectionsArray = jsonObject.getJSONArray("sections")
                for (j in 0 until sectionsArray.length()) {
                    val sectionJSONObject = sectionsArray.getJSONObject(j)
                    sections.add(RouteSection(
                        vehicleName = sectionJSONObject.getString("vehicleName"),
                        startTime = LocalDateTime.parse(jsonObject.getString("startTime"), formatter),
                        startStation = sectionJSONObject.getString("startStation"),
                        endTime = LocalDateTime.parse(jsonObject.getString("endTime"), formatter),
                        endStation = sectionJSONObject.getString("endStation"),
                    ))
                }
                result.add(Route(
                    startStation = jsonObject.getString("startStation"),
                    endStation = jsonObject.getString("endStation"),
                    startTime = LocalDateTime.parse(jsonObject.getString("startTime"), formatter),
                    endTime = LocalDateTime.parse(jsonObject.getString("endTime"), formatter),
                    sections = sections
                ))
            }
            return result
        }
        else{
            return null
        }
    }
}


//Definition of the database. It consists of ConfigurationEntity Table and EventEntity Table
//eportSchema doesn't work... I don't know why but I want to keep it anyways, hoping the schema will be randomly exported to the project directory anytime soon
//Version needs to be updated everytime something changes in the table structure. This will destroy all data because of fallbackToDestructiveMigration
@Database(entities = [Configuration::class, Event::class], version = 19,exportSchema = true)
@TypeConverters(DataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmConfigurationDao(): ConfigurationHandler.ConfigurationDao
    abstract fun eventConfigurationDao(): EventHandler.ConfigurationDao

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
        @Suppress("unused")
        fun logDatabaseSchema(db: RoomDatabase) {
            val tableCursor = db.query("SELECT name FROM sqlite_master WHERE type='table'", null)
            while (tableCursor.moveToNext()) {
                val tableName = tableCursor.getString(0)
                Logger(null).log(Logger.Level.INFO,"Table: $tableName")

                val columnCursor = db.query("PRAGMA table_info($tableName)", null)
                while (columnCursor.moveToNext()) {
                    val columnName = columnCursor.getString(columnCursor.getColumnIndexOrThrow("name"))
                    val columnType = columnCursor.getString(columnCursor.getColumnIndexOrThrow("type"))
                    Logger(null).log(Logger.Level.INFO,"-- Column: $columnName ($columnType)")
                }
                columnCursor.close()
            }
            tableCursor.close()
        }
    }
}
