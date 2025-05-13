package de.heinzenburger.g2_weckmichmal.specifications

/**
 * Interface for defining the logic used to calculate the next wake-up event
 * based on user-defined configuration settings.
 */
interface WakeUpCalculationSpecification {

    /**
     * Calculates the next wake-up event using the specified configuration.
     *
     * The calculation includes:
     * - Selecting the next applicable date based on active days.
     * - Determining the expected arrival time (e.g., via course schedules or a fixed arrival time).
     * - Calculating the departure time using the selected route and estimated travel duration.
     * - Subtracting the configured wake-up buffer to determine the final wake-up time.
     *
     * @param configuration The [Configuration] object containing alarm parameters,
     * such as active days, buffers, station details, and travel preferences.
     * @return An [Event] containing the calculated wake-up time, associated date,
     * relevant courses, and route details.
     * @throws WakeUpCalculatorException if the calculation fails due to invalid or missing data.
     */
    @Throws(WakeUpCalculatorException::class)
    fun calculateNextEvent(configuration: Configuration): Event
}


/**
 * Represents a sealed hierarchy of exceptions that may occur during wake-up time calculations.
 */
sealed class WakeUpCalculatorException(message: String?, cause: Throwable?) :
    Throwable(message, cause) {

    /**
     * Thrown when no course data is available for the given configuration or date range.
     */
    class NoCoursesFound : CourseFetcherException("No courses found", null)

    /**
     * Thrown when a connection error occurs while fetching course data.
     *
     * @param cause The underlying exception causing the connection failure.
     */
    class CoursesConnectionError(cause: Throwable?) :
        CourseFetcherException("Could not fetch courses due to a connection error", cause)

    /**
     * Thrown when the fetched course data is malformed or cannot be parsed.
     *
     * @param cause The underlying exception related to data format issues.
     */
    class CoursesInvalidDataFormatError(cause: Throwable?) :
        CourseFetcherException("Invalid data format in fetched course data", cause)

    /**
     * Thrown when an unexpected or logically impossible state is encountered.
     */
    class InvalidStateException :
        CourseFetcherException("An unexpected internal state was reached", null)

    // TODO: Implement error handling for RoutePlanner-related failures
}