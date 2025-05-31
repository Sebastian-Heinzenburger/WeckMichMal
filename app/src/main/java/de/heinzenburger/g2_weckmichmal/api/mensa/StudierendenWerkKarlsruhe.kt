package de.heinzenburger.g2_weckmichmal.api.mensa

import de.heinzenburger.g2_weckmichmal.specifications.MealType
import de.heinzenburger.g2_weckmichmal.specifications.MealType.MEAT
import de.heinzenburger.g2_weckmichmal.specifications.MealType.VEGAN
import de.heinzenburger.g2_weckmichmal.specifications.MealType.VEGETARIAN
import de.heinzenburger.g2_weckmichmal.specifications.MensaFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL

typealias HTMLString = String

/**
 * Fetcher implementation for StudierendenWerk Karlsruhe Mensa.
 * Scrapes the HTML page to extract meal information for the next available day.
 */
class StudierendenWerkKarlsruhe(
    private val mensaUrl: URL = URL("https://www.sw-ka.de/de/hochschulgastronomie/speiseplan/mensa_erzberger/?view=ok&c=erzberger&STYLE=popup_plain")
) : MensaFetcherSpecification {

    /**
     * Fetches the meals for the next available day from the Mensa's HTML page from the given `mensaUrl`.
     * Returns <strong>only</strong> the meals for the next day, even when there already is data
     * for later dates.
     * @return A list of MensaMeal objects representing today's meals.
     */
    override fun nextMeals(): List<MensaMeal> {
        val mealTablesOfNextAvailableDay: Elements = fetchMealTablesOfNextAvailableDay()
        val meals = parseMealTables(mealTablesOfNextAvailableDay)
        return meals
    }

    /**
     * Parses all meal tables and returns a flat list of MensaMeal objects.
     * @param tables Elements containing meal tables.
     * @return List of MensaMeal objects.
     */
    private fun parseMealTables(tables: Elements): List<MensaMeal> {
        return tables.flatMap { table ->
            mealsOfTable(table) ?: emptyList()
        }
    }

    /**
     * Parses a single meal table and returns a list of MensaMeal objects, or null if the table is not a meal table.
     * @param table The HTML table element.
     * @return List of MensaMeal objects or null.
     */
    private fun mealsOfTable(table: Element): List<MensaMeal>? {
        if (!table.hasClass("easy-tab-dot")) return null
        val rows = table.select("tr")
        return rows.mapNotNull { row -> mealOfRow(row) }
    }

    /**
     * Parses a single row of a meal table and returns a MensaMeal object, or null if the row is not valid.
     * @param row The HTML row element.
     * @return MensaMeal object or null.
     */
    private fun mealOfRow(row: Element): MensaMeal? {
        val cells = row.select("td")
        if (cells.size < 2) return null

        val mealName = cells[1].text().replace(
            Regex("\\s*\\([^)]*\\)"), // remove the info in parenthesis at the end of the meal name
            ""
        ).trim()

        val priceText = cells.getOrNull(2)?.text() ?: "0,00"
        val price = priceText.replace(",", ".") // needed because we don't do localization
            .replace("€", "").trim().toDoubleOrNull() ?: 0.0

        val mealTypeText = cells[0].text()
        val mealType = parseMealType(mealTypeText)

        return MensaMeal(mealName, price, mealType)
    }

    /**
     * Fetches the meal tables for the next available day by extracting the relevant HTML section.
     * @return Elements containing the meal tables.
     */
    private fun fetchMealTablesOfNextAvailableDay(): Elements {
        val nextDayTable = fetchMultiMealTableOfNextAvailableDay()
        if(nextDayTable != null){
            val mealTables = nextDayTable.select("table")
            return mealTables
        }
        else{
            return Elements()
        }
    }

    /**
     * Fetches the main table containing all meals for the next available day.
     * @return The HTML element of the main meal table, or null if not found.
     */
    private fun fetchMultiMealTableOfNextAvailableDay(): Element? {
        val mensaPage = fetchMensaPage()
        val nextDayTable = extractFirstMultiMealTable(mensaPage)
        return nextDayTable
    }

    /**
     * Extracts the first multi-meal table from the Mensa HTML page.
     * @param mensaPage The parsed HTML document.
     * @return The HTML element of the first multi-meal table, or null if not found.
     */
    private fun extractFirstMultiMealTable(mensaPage: Document): Element? {
        return try {
            mensaPage.select("table").first {
                it.hasAttr("border") && it.hasAttr("width")
            }
        }
        catch (_: NoSuchElementException){
            return null
        }
    }

    /**
     * Fetches and parses the Mensa page as a Jsoup Document.
     * @return The parsed HTML Document of the Mensa page.
     */
    private fun fetchMensaPage(): Document {
        val page: HTMLString = fetchMensaPageString()
        return Jsoup.parse(page)
    }

    /**
     * Fetches the HTML content of the Mensa page as a string.
     * @return The HTML content of the Mensa page.
     */
    private fun fetchMensaPageString(): HTMLString {
        return mensaUrl.readText()
    }

    /**
     * Maps a string to a MealType based on keywords.
     * @param type The string to map.
     * @return The corresponding MealType.
     */
    fun parseMealType(type: String): MealType {
        return when {
            type.contains("[VEG]", ignoreCase = true) -> VEGAN
            type.contains("[VG]", ignoreCase = true) -> VEGETARIAN
            else -> MEAT
        }
    }

}
