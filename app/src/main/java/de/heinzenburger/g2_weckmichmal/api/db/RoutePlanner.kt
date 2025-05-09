package de.heinzenburger.g2_weckmichmal.api.db

import de.heinzenburger.g2_weckmichmal.specifications.I_RoutePlannerSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Route
import de.heinzenburger.g2_weckmichmal.specifications.RouteSection
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException
import kotlin.jvm.Throws

typealias StationID = String

class RoutePlanner : I_RoutePlannerSpecification {

    var DB_API_BASE_URL_WITHOUT_SLASH_IN_THE_END = "https://www.bahn.de/web/api"

    private fun fetchStations(stationNameQuery: String): JSONArray {
        val urlEncodedStationName = URLEncoder.encode(stationNameQuery, "UTF-8")
        val url =
            URL("${DB_API_BASE_URL_WITHOUT_SLASH_IN_THE_END}/reiseloesung/orte?suchbegriff=${urlEncodedStationName}&typ=ALL&limit=10")
        val connection = url.openConnection()
        val response = connection.getInputStream().bufferedReader().use { it.readText() }
        return JSONArray(response)
    }

    private fun fetchStationID(stationName: String): StationID {
        val stations = fetchStations(stationName)
        val firstResult = stations.getJSONObject(0)
        return firstResult.getString("id")
    }

    @Throws(JSONException::class, IOException::class, ZipException::class)
    override fun planRoute(
        startStation: String, endStation: String, timeOfArrival: LocalDateTime
    ): List<Route> {

        val url = URL("${DB_API_BASE_URL_WITHOUT_SLASH_IN_THE_END}/angebote/fahrplan")
        val httpConnection = url.openConnection()
        httpConnection.setRequestProperty("Accept", "application/json")
        httpConnection.setRequestProperty("Content-Type", "application/json")

        httpConnection.doOutput = true
        httpConnection.getOutputStream().use {
            val startStationID = fetchStationID(startStation)
            val endStationID = fetchStationID(endStation)
            val dbDAteTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val timeOfArrivalString = timeOfArrival.format(dbDAteTimeFormatter)
            it.write(
                """
            {
                "abfahrtsHalt":"$startStationID",
                "anfrageZeitpunkt":"$timeOfArrivalString",
                "ankunftsHalt":"$endStationID",
                "ankunftSuche":"ANKUNFT",
                "klasse":"KLASSE_2",
                "produktgattungen":["REGIONAL","SBAHN","ICE","BUS","SCHIFF","UBAHN","TRAM"],
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
        val jsonResponse = JSONObject(response)

        val routes = jsonResponse.getJSONArray("verbindungen")

        val results = mutableListOf<Route>()
        for (i in 0 until routes.length()) {
            val route = routes.getJSONObject(i)
            val potentialRoute = ParsingUtilities.parsePotentialRouteFromJSON(route)
            results.add(potentialRoute)
        }
        return results
    }

    override fun deriveValidStationNames(stationName: String): List<String> {
        val stations = fetchStations(stationName)

        val stationNames = mutableListOf<String>()
        for (i in 0 until stations.length()) {
            val station = stations.getJSONObject(i)
            stationNames.add(station.getString("name"))
        }
        return stationNames
    }


    private class ParsingUtilities {
        companion object {

            fun parsePotentialRouteFromJSON(route: JSONObject): Route {
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