package de.heinzenburger.g2_weckmichmal.api.mensa

import org.junit.Test

class StudierendenWerkKarlsruheTest {
    @Test
    fun `nextMeals is not empty`() {
        val mensaFetcher = StudierendenWerkKarlsruhe()
        val meals = mensaFetcher.nextMeals()
        assert(meals.isNotEmpty()) { "Expected non-empty HTML response from Mensa page" }
    }

    @Test
    fun `nextMeals has at least one price`() {
        val mensaFetcher = StudierendenWerkKarlsruhe()
        val meals = mensaFetcher.nextMeals()
        assert(meals.any {it.price > 0}) { "Expected at least one meal with a price greater than 0"}
    }
}