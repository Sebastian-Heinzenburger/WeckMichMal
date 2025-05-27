package de.heinzenburger.g2_weckmichmal.api.mensa

import org.junit.Test

class StudierendenWerkKarlsruheTest {
    @Test
    fun `nextMeals doesnt throw exception`() {
        val mensaFetcher = StudierendenWerkKarlsruhe()
        val html = mensaFetcher.nextMeals()
        assert(html.isNotEmpty()) { "Expected non-empty HTML response from Mensa page" }
        assert(html.any { it.name.contains("Thunfisch") }) { "Expected Thunfisch" }
    }
}