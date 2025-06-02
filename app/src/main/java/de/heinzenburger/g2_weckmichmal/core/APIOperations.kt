package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.api.mensa.StudierendenWerkKarlsruhe
import de.heinzenburger.g2_weckmichmal.api.routes.DBRoutePlanner
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherException
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import java.net.URL

class APIOperations(val core: Core) {
    fun getListOfNameOfCourses(): List<String> {
        try {
            val url = core.getRaplaURL()
            if(url != ""){
                val courseFetcher = RaplaFetcher(
                    raplaUrl = URL(
                        url
                    ),
                    excludedCourseNames = core.getListOfExcludedCourses()!!.toSet()
                )
                return courseFetcher.getAllCourseNames()
            }
        }
        catch (e : CourseFetcherException){
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        return emptyList()
    }

    fun nextMensaMeals(): List<MensaMeal> {
        return StudierendenWerkKarlsruhe().nextMeals()
    }

    fun deriveStationName(input: String): List<String> {
        core.log(Logger.Level.INFO, "deriveStationName called with input: $input")
        val routePlanner = DBRoutePlanner()
        val result = routePlanner.deriveValidStationNames(input)
        core.log(Logger.Level.INFO, "deriveStationName result: $result")
        return result
    }
}