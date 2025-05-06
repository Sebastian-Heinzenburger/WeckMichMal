package de.heinzenburger.g2_weckmichmal.specifications

/**
 * Interface defining the core
 */
interface I_Core{
    /*
    To be implemented: Does not have any state, needs context as parameter
     */

    /**
     * Update all events depending on current train and rapla status
     */
    fun deriveStationName(input: String) : List<String>

    /**
     * Update all events depending on current train and rapla status
     */
    fun runUpdateLogic()

    /**
     * Start the background process that wakes the user at the time of next occuring event
     */
    fun runWakeUpLogic()

    /**
     * Start the background process that regularly updates the events
     */
    fun startUpdateScheduler()

    /**
     * Save the rapla URL to the settings database
     * @param url RAPLA URL to be saved in the settings database
     */
    fun saveRaplaURL(url : String)

    /**
     * Get RAPLA URL from settings database
     */
    fun getRaplaURL(): String?

    /**
     * Update attribute active in a [ConfigurationEntity]
     * @param isActive stores whether the configuration should be active
     * @param configurationEntity the [ConfigurationEntity] to be updated
     */
    fun updateConfigurationActive(isActive: Boolean, configurationEntity: ConfigurationEntity)
    /**
     * @return Returns true if application is opened for the first time since installation
     */
    fun isApplicationOpenedFirstTime() : Boolean?

    /**
     *  Generates the next Event for this configuration and saves both into the databas
     *  @param configurationEntity Configuration to be stored into the database
     */
    fun generateOrUpdateAlarmConfiguration(configurationEntity: ConfigurationEntity)

    /**
     * Get all configurations and their corresponding event
     * @return returns a List of [ConfigurationAndEventEntity] which contain a configurationEntity and an eventEntity
     */
    fun getAllConfigurationAndEvent() : List<ConfigurationAndEventEntity>?

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
     * If configuration entity contains only valid attributes
     */
    fun validateConfigurationEntity(configurationEntity: ConfigurationEntity) : Boolean

    /**
     * Set Welcome Screen
     */
    fun setWelcomeScreen()

    /**
     * Set Settings Screen
     */
    fun setSettingsScreen()

    /**
     * Set AlarmClockOverview Screen
     */
    fun setAlarmClockOverviewScreen()

    /**
     * Set Information Screen
     */
    fun setInformationScreen()

    /**
     * Set AlarmClockEdit Screen
     */
    fun setAlarmClockEditScreen()

    /**
     * Show a Toast message
     */
    fun showToast(message: String)
}