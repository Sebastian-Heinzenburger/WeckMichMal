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
 * Represents a sealed hierarchy of exceptions that can occur during wake-up time calculations.
 */
sealed class WakeUpCalculatorException(message: String?, cause: Throwable?) :
    Throwable(message, cause) {

    /**
     * Thrown when no course data is found for the given configuration or date range.
     */
    class NoCoursesFound : WakeUpCalculatorException("No courses found", null)

    /**
     * Thrown when a network or server issue occurs while attempting to fetch course data.
     *
     * @param cause The underlying exception that caused the connection failure.
     */
    class CoursesConnectionError(cause: Throwable?) :
        WakeUpCalculatorException("Failed to fetch courses due to a connection error", cause)

    /**
     * Thrown when the fetched course data is malformed, incomplete, or cannot be parsed.
     *
     * @param cause The underlying exception related to the data parsing or format issue.
     */
    class CoursesInvalidDataFormatError(cause: Throwable?) :
        WakeUpCalculatorException("Received invalid or unparsable course data", cause)

    /**
     * Thrown when an unexpected or logically invalid state is encountered during execution.
     */
    class InvalidStateException :
        WakeUpCalculatorException("An unexpected internal state was encountered", null)

    /**
     * Thrown when the provided configuration is invalid or incomplete.
     *
     * @param message A descriptive message indicating the nature of the configuration issue.
     */
    class InvalidConfiguration(message: String) :
        WakeUpCalculatorException(message, null)

    /**
     * Thrown when no valid routes can be found with the given configuration and arrival time.
     */
    class NoRoutesFound :
        WakeUpCalculatorException("No routes found with the provided configuration", null)

    /**
     * Thrown when the route configuration is invalid or malformed.
     *
     * @param cause The underlying exception that caused the failure.
     */
    class RouteInvalidConfiguration(cause: Throwable?) :
        WakeUpCalculatorException("Invalid route configuration", cause)

    /**
     * Thrown when a network or connection issue occurs during route planning.
     *
     * @param cause The underlying exception that caused the connection failure.
     */
    class RouteConnectionError(cause: Throwable?) :
        WakeUpCalculatorException("Failed to fetch routes due to a connection error", cause)

    /**
     * Thrown when the response from the route planner has an invalid or unrecognized format.
     *
     * @param cause The underlying exception related to the format issue.
     */
    class RouteInvalidResponse(cause: Throwable?) :
        WakeUpCalculatorException("Invalid response format from route planner", cause)
}
