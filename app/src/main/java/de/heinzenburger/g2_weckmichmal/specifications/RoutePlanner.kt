package de.heinzenburger.g2_weckmichmal.specifications

import java.time.LocalDateTime

/**
 * Interface defining the behavior of the Route Planner.
 */
interface RoutePlannerSpecification {
    /**
     * Plans a list of possible routes that fulfill the given conditions.
     *
     * @param startStation The DB Navigator conform name of the starting station for the journey.
     * @param endStation The DB Navigator conform name of the destination station for the journey.
     * @param timeOfArrival The desired time of arrival at the destination station.
     * @param strict If `true` (default), only routes that are strictly guaranteed to arrive *before or at* the specified
     *               arrival time will be returned, without considering currently unavailable or past connections.
     *               If `false`, a best-effort approach is used to find routes that ideally arrive before the desired time,
     *               potentially excluding early connections that are no longer feasible based on the current time.
     *
     * @return A list of [Route] objects that represent possible routes from the start station to the end station.
     *
     * @throws RoutePlannerException.MalformedStationNameException if the station name is malformed.
     * @throws RoutePlannerException.NetworkException if there is a network error.
     * @throws RoutePlannerException.InvalidResponseFormatException if the response format is invalid.
     */
    fun planRoute(
        startStation: String,
        endStation: String,
        timeOfArrival: LocalDateTime,
        strict: Boolean = true
    ): List<Route>

    /**
     * Derives a list of valid station names from a potentially invalid station name.
     *
     * @param stationName The station name that may be invalid or incomplete.
     * @return A list of valid station names that match or correct the given station name.
     * @throws RoutePlannerException.MalformedStationNameException if the station name is malformed.
     * @throws RoutePlannerException.NetworkException if there is a network error.
     * @throws RoutePlannerException.InvalidResponseFormatException if the response format is invalid.
     */
    fun deriveValidStationNames(stationName: String): List<String>
}

/**
 * Represents a route for a given start station and end station.
 *
 * @property startStation The DB Navigator conform name of the start station for the whole journey.
 * @property endStation The DB Navigator conform name of the end station for the whole journey.
 * @property startTime The start date and time for the entire journey.
 * @property endTime The end date and time for the entire journey.
 * @property sections The list of route sections that make up the entire route.
 */
data class Route(

    /** The DB Navigator conform name of the start station for the whole journey. */
    val startStation: String,

    /** The DB Navigator conform name of the end station of the whole journey */
    val endStation: String,

    /** The start date and time for the entire route journey. */
    val startTime: LocalDateTime,

    /** The end date and time for the entire route journey. */
    val endTime: LocalDateTime,

    /** The list of route sections that the entire route consists of. */
    val sections: List<RouteSection>
)

/**
 * Represents a section of the route (where the user stays in the same vehicle) specified by the DB Navigator.
 *
 * @property vehicleName The DB Navigator conform name of the vehicle for this section of the journey.
 * @property startTime The time the route section starts.
 * @property startStation The DB Navigator conform name of the start station for this section of the journey.
 * @property endTime The time the route section ends.
 * @property endStation The DB Navigator conform name of the end station for this section of the journey.
 */
data class RouteSection(

    /** The DB Navigator conform name of the vehicle for this section of the journey. */
    val vehicleName: String,

    /** The time the route section starts. */
    val startTime: LocalDateTime,

    /** The DB Navigator conform name of the start station for this section. */
    val startStation: String,

    /** The time the route section ends. */
    val endTime: LocalDateTime,

    /** The DB Navigator conform name of the end station for this section. */
    val endStation: String,
)

sealed class RoutePlannerException(message: String, cause: Exception?) : Exception(message, cause) {
    class MalformedStationNameException(stationName: String, cause: Exception?) :
        RoutePlannerException("The Station Name '$stationName' is malformed!", cause)

    class NetworkException(cause: Exception?) :
        RoutePlannerException("Network error occurred!", cause)

    class InvalidResponseFormatException(cause: Exception?) :
        RoutePlannerException("The response format is invalid!", cause)
}