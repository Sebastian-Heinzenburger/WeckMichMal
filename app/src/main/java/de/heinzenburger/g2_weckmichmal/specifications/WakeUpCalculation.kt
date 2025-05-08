package de.heinzenburger.g2_weckmichmal.specifications

/**
 * Interface defining the behavior for calculating the next wake-up time based on configuration settings.
 */
interface WakeUpCalculationSpecification {

    /**
     * Calculates the next wake-up event based on the provided configuration.
     *
     * This involves:
     * - Selecting the next valid date according to the configured active days.
     * - Identifying the arrival time at the destination (e.g., based on course schedules or a fixed time).
     * - Calculating the required departure time, accounting for route and travel time.
     * - Subtracting the configured wake-up buffer to determine the final wake-up time.
     *
     * @param configuration The [Configuration] containing alarm settings, including buffers,
     * travel preferences, station details, and active days.
     * @return The calculated [Event], which includes the wake-up time, event date,
     * associated courses, and travel routes.
     * @throws Exception if the calculation fails (e.g., due to missing course data or invalid configuration).
     */
    @Throws(Exception::class)
    fun calculateNextEvent(configuration: Configuration): Event

    /**
     * Calculates the next wake-up events for multiple configurations in batch.
     *
     * For each configuration:
     * - Determines the next valid date based on active days.
     * - Calculates arrival, departure, and wake-up times.
     * - Groups relevant course and route information into the result.
     *
     * Batch processing can optimize shared operations, such as bulk course fetching or route calculations.
     *
     * @param configurations A list of [Configuration] objects to process.
     * @return A list of calculated [Event]s, one for each configuration.
     * @throws Exception if the calculation fails for any configuration.
     */
    @Throws(Exception::class)
    fun batchCalculateNextEvent(configurations: List<Configuration>): List<Event>
}