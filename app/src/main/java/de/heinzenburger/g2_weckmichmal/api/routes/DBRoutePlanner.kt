package de.heinzenburger.g2_weckmichmal.api.routes

import de.heinzenburger.g2_weckmichmal.specifications.Route
import de.heinzenburger.g2_weckmichmal.specifications.RoutePlannerException
import de.heinzenburger.g2_weckmichmal.specifications.RoutePlannerSpecification
import de.heinzenburger.g2_weckmichmal.specifications.RouteSection
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException

typealias StationID = String

class DBRoutePlanner : RoutePlannerSpecification {

    @Throws(RoutePlannerException::class)
    override fun deriveValidStationNames(stationName: String): List<String> {
        try {
            val stations = DBJsonApi.fetchStations(stationName)
            val stationNames = ParsingUtilities.parseStationNamesFromJSON(stations)
            return stationNames
        } catch (e: IOException) {
            throw RoutePlannerException.NetworkException(e)
        } catch (e: JSONException) {
            throw RoutePlannerException.InvalidResponseFormatException(e)
        }
    }

    @Throws(RoutePlannerException::class)
    override fun planRoute(
        startStationName: String,
        destinationStationName: String,
        timeOfArrival: LocalDateTime,
        strictArrivalTime: Boolean
    ): List<Route> {
        try {
            val routesResponse: JSONArray = DBJsonApi.fetchRoutes(
                stationID(startStationName),
                stationID(destinationStationName),
                timeOfArrival
            )
            val routes = ParsingUtilities.parseRoutesFromJSON(routesResponse)
            return if (strictArrivalTime) {
                val arrivesOnTime = { route: Route -> !route.endTime.isAfter(timeOfArrival) } // Inverse isAfter to include isEqual and isBefore
                routes.filter(arrivesOnTime)
            } else {
                routes
            }
        } catch (e: IOException) {
            throw RoutePlannerException.NetworkException(e)
        } catch (e: ZipException) {
            throw RoutePlannerException.InvalidResponseFormatException(e)
        } catch (e: JSONException) {
            throw RoutePlannerException.InvalidResponseFormatException(e)
        } catch (e: DateTimeParseException) {
            throw RoutePlannerException.InvalidResponseFormatException(e)
        }
    }

    @Throws(
        IOException::class,
        JSONException::class,
        RoutePlannerException.MalformedStationNameException::class
    )
    private fun stationID(stationName: String): StationID {
        val stations = DBJsonApi.fetchStations(stationName)
        val firstResult = stations.getJSONObject(0)
        return firstResult.getString("id")
    }

    private class DBJsonApi {
        companion object {
            var DB_API_BASE_URL_WITHOUT_SLASH_IN_THE_END = "https://www.bahn.de/web/api"

            @Throws(
                IOException::class,
                JSONException::class,
                RoutePlannerException.MalformedStationNameException::class
            )
            fun fetchStations(stationNameQuery: String): JSONArray {
                try {
                    val urlEncodedStationName = URLEncoder.encode(stationNameQuery, "UTF-8")
                    val url = URL("${DB_API_BASE_URL_WITHOUT_SLASH_IN_THE_END}/reiseloesung/orte?suchbegriff=${urlEncodedStationName}&typ=ALL&limit=10")
                    val connection = url.openConnection()
                    val response = connection.getInputStream().bufferedReader().use { it.readText() }
                    return JSONArray(response)
                } catch (e: MalformedURLException) {
                    throw RoutePlannerException.MalformedStationNameException(stationNameQuery, e)
                } catch (e: UnsupportedEncodingException) {
                    throw RoutePlannerException.MalformedStationNameException(stationNameQuery, e)
                }
            }

            @Throws(
                IOException::class,
                DateTimeException::class,
                ZipException::class,
                JSONException::class
            )
            fun fetchRoutes(
                start: StationID, destination: StationID, timeOfArrival: LocalDateTime
            ): JSONArray {
                val url = URL("${DB_API_BASE_URL_WITHOUT_SLASH_IN_THE_END}/angebote/fahrplan")
                val httpConnection = url.openConnection()
                httpConnection.setRequestProperty("Accept", "application/json")
                httpConnection.setRequestProperty("Content-Type", "application/json")

                // we want to send a request body, so we need to set this to true
                httpConnection.doOutput = true
                httpConnection.getOutputStream().use {
                    val dbDAteTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    val timeOfArrivalString = timeOfArrival.format(dbDAteTimeFormatter)
                    it.write(
                        """
                    {
                        "abfahrtsHalt":"$start",
                        "anfrageZeitpunkt":"$timeOfArrivalString",
                        "ankunftsHalt":"$destination",
                        "ankunftSuche":"ANKUNFT",
                        "klasse":"KLASSE_2",
                        "produktgattungen":["REGIONAL","SBAHN","BUS","SCHIFF","UBAHN","TRAM"],
                        "reisende":[{"typ":"ERWACHSENER","ermaessigungen":[{"art":"KEINE_ERMAESSIGUNG","klasse":"KLASSENLOS"}],"alter":[],"anzahl":1}],
                        "schnelleVerbindungen":true,
                        "sitzplatzOnly":false,
                        "bikeCarriage":false,
                        "reservierungsKontingenteVorhanden":false,
                        "nurDeutschlandTicketVerbindungen":false,
                        "deutschlandTicketVorhanden":false
                    }
                    """.toByteArray()
                    )
                }

                // In the app, the android system handles gzip encoding transparently.
                // This means we can just read the input stream directly.
                // However, in unit tests, we have to manually handle gzip encoding.
                // For more information, see https://stackoverflow.com/a/42346308
                val responseStream = if (httpConnection.contentEncoding == "gzip") {
                    GZIPInputStream(httpConnection.getInputStream())
                } else {
                    httpConnection.getInputStream()
                }
                val response = responseStream.bufferedReader().use { it.readText() }
                val routes = JSONObject(response).getJSONArray("verbindungen")
                return routes
            }
        }
    }


    private class ParsingUtilities {
        companion object {

            @Throws(JSONException::class)
            fun parseStationNamesFromJSON(stations: JSONArray): List<String> {
                val stationNames = mutableListOf<String>()
                for (i in 0 until stations.length()) {
                    val station = stations.getJSONObject(i)
                    stationNames.add(station.getString("name"))
                }
                return stationNames
            }

            @Throws(JSONException::class, DateTimeParseException::class, NoSuchFileException::class)
            fun parseRoutesFromJSON(routes: JSONArray): MutableList<Route> {
                val results = mutableListOf<Route>()
                for (i in 0 until routes.length()) {
                    val route = routes.getJSONObject(i)
                    val potentialRoute = parseRouteFromJSON(route)
                    results.add(potentialRoute)
                }
                return results
            }

            @Throws(JSONException::class, DateTimeParseException::class, NoSuchFileException::class)
            private fun parseRouteFromJSON(route: JSONObject): Route {
                val sections = route.getJSONArray("verbindungsAbschnitte")
                val routeSections = mutableListOf<RouteSection>()
                for (j in 0 until sections.length()) {
                    val section = sections.getJSONObject(j)
                    val routeSection = parseRouteSectionFromJSON(section)
                    routeSections.add(routeSection)
                }

                val firstSection = routeSections.first()
                val lastSection = routeSections.last()
                return Route(
                    startStation = firstSection.startStation,
                    endStation = lastSection.endStation,
                    startTime = firstSection.startTime,
                    endTime = lastSection.endTime,
                    sections = routeSections,
                )
            }

            @Throws(JSONException::class, DateTimeParseException::class)
            private fun parseRouteSectionFromJSON(section: JSONObject): RouteSection {
                val trainName = section.getJSONObject("verkehrsmittel").getString("name")

                val startTime = LocalDateTime.parse(section.getString("abfahrtsZeitpunkt"))
                val endTime = LocalDateTime.parse(section.getString("ankunftsZeitpunkt"))

                val startStation = section.getString("abfahrtsOrt")
                val endStation = section.getString("ankunftsOrt")

                return RouteSection(trainName, startTime, startStation, endTime, endStation)
            }
        }
    }
}