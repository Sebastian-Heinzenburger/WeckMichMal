package de.heinzenburger.g2_weckmichmal.specifications

/**
 * Interface for fetching meals from a Mensa (university cafeteria).
 * This interface defines the contract for fetching meals for the next available day.
 */
interface I_MensaFetcher {

    /**
     * Fetches the meals for the next available day from the Mensa's API, HTML page or database.
     * Returns <strong>only</strong> the meals for the next day, even when there already is data
     * for later dates.
     * @return A list of MensaMeal objects representing today's meals.
     */
    fun nextMeals(): List<MensaMeal>

}

/**
 * Represents a meal in the Mensa.
 * @param name The name of the meal.
 * @param price The price of the meal.
 * @param isVegetarian Indicates if the meal is vegetarian.
 * @param isVegan Indicates if the meal is vegan.
 */
data class MensaMeal(
    val name: String,
    val price: Double,
    val isVegetarian: Boolean,
    val isVegan: Boolean
)
