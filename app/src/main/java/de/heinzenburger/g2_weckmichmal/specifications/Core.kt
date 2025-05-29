package de.heinzenburger.g2_weckmichmal.specifications

import de.heinzenburger.g2_weckmichmal.persistence.Logger
import java.time.LocalDate

/**
 * Interface defining the core
 */
interface CoreSpecification {
    /**
     * Update all events depending on current train and rapla status
     */
    fun deriveStationName(input: String) : List<String>

    /**
     * Returns the meals for the next day
     * @return A list of MensaMeal objects representing today's meals.
     */
    fun nextMensaMeals() : List<MensaMeal>

    /**
     * Update all events depending on current train and rapla status
     */
    fun runUpdateLogic()

    /**
     * Start the background process that wakes the user at the time of next occuring event
     */
    fun runWakeUpLogic(event: Event)

    /**
     * Start the background process that regularly updates the events
     */
    fun startUpdateScheduler(delay: Int)

    /**
     * Save the rapla URL to the settings database
     * @param url RAPLA URL to be saved in the settings database
     */
    fun saveRaplaURL(url : String)

    /**
     * Save the rapla URL to the settings database
     * @param director of the course
     * @param course id
     */
    fun saveRaplaURL(director : String, course: String)


    /**
     * Get RAPLA URL from settings database
     */
    fun getRaplaURL(): String?

    /**
     * Return true if Phone is connected to the Internet
     */
    fun isInternetAvailable(): Boolean

    /**
     * Logs [text] to log file and LogCat
     */
    fun log(level: Logger.Level, text: String)

    /**
     * Retrieves Log from database
     */
    fun getLog() : String

    /**
     * Update attribute active in a [Configuration]
     * @param isActive stores whether the configuration should be active
     * @param configuration the [Configuration] to be updated
     */
    fun updateConfigurationActive(isActive: Boolean, configuration: Configuration)

    /**
     * Update attribute ichHabGeringt in a [Configuration]
     * @param date stores the date when the configuration rang last time
     * @param uid of the [Configuration] to be updated
     */
    fun updateConfigurationIchHabGeringt(date: LocalDate, uid: Long)

    /**
     * @return Returns true if application is opened for the first time since installation
     */
    fun isApplicationOpenedFirstTime() : Boolean?

    /**
     *  Generates the next Event for this configuration and saves both into the databas
     *  @param configuration Configuration to be stored into the database
     */
    fun generateOrUpdateAlarmConfiguration(configuration: Configuration)

    /**
     * Get all configurations and their corresponding event
     * @return returns a List of [ConfigurationWithEvent] which contain a configurationEntity and an eventEntity
     */
    fun getAllConfigurationAndEvent() : List<ConfigurationWithEvent>?

    /**
     * Delete an alarm configuration and its associated event
     * @param uid the uid of the configuration that should be deleted from the database
     */
    fun deleteAlarmConfiguration(uid: Long)

    /**
     * Is Valid Course URL
     */
    fun isValidCourseURL(urlString : String) : Boolean

    /**
     * Is Valid Course URL
     */
    fun isValidCourseURL(director : String, course: String) : Boolean

    /**
     * Get all courses that can be found in Course URL within next 3 months
     */
    fun getListOfCourses(): List<Course>

    /**
     * Get the list of excluded courses from the database
     */
    fun getListOfExcludedCourses() : List<Course>

    /**
     * Save the list of excluded courses into the database
     */
    fun updateListOfExcludedCourses(excludedCoursesList: List<Course>)

    /**
     * Reads the default alarm values from the database
     */
    fun getDefaultAlarmValues() : SettingsEntity.DefaultAlarmValues

    /**
     * Update default alarm values in database
     */
    fun updateDefaultAlarmValues(defaultAlarmValues: SettingsEntity.DefaultAlarmValues)

    /**
     * If configuration entity contains only valid attributes
     */
    fun validateConfiguration(configuration: Configuration) : Boolean

    /**
     * Show a Toast message
     */
    fun showToast(message: String)
}