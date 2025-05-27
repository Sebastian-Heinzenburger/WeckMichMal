package de.heinzenburger.g2_weckmichmal.api.mensa

import de.heinzenburger.g2_weckmichmal.specifications.MensaFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL

typealias HTMLString = String

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
        val mensaPage = fetchMensaPage()
        val meals: MutableList<MensaMeal> = ArrayList()

        val tablesInPage = mensaPage.select("table")
        val firstTableWithMeals = tablesInPage.first {
            it.hasClass("easy-tab-dot")
        }

        val rows = firstTableWithMeals.select("tr")
        rows.forEach { row ->
            val cells = row.select("td")
            if (cells.size >= 2) {
                val mealType = cells[0].text()
                val mealName = cells[1].text()
                if (mealName.trim() == "-") return@forEach

                val priceText = cells.getOrNull(2)?.text() ?: "0,00"
                val price = priceText.replace("€", "")
                    .replace(",", ".") // needed because we don't do localization
                    .trim().toDoubleOrNull() ?: 0.0
                val isVegetarian = mealType.contains("[VG]", ignoreCase = true)
                val isVegan = mealType.contains("[VEG]", ignoreCase = true)

                val meal = MensaMeal(mealName, price, isVegetarian, isVegan)
                println(meal)
                meals.add(meal);
            }
        }
        return meals
    }

    private fun fetchMensaPage(): Document {
        val htmlText = fetchMensaPageContent()
        return Jsoup.parse(htmlText)
    }

    /**
     * Fetches the meals for today from the Mensa HTML page.
     * @returns A list of MensaMeal objects representing today's meals.
     */
    private fun fetchMensaPageContent(): HTMLString {
        return mensaUrl.readText()
    }

}
