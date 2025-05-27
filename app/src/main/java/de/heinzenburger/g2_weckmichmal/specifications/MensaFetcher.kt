package de.heinzenburger.g2_weckmichmal.specifications

/**
 * Interface for fetching meals from a Mensa (university cafeteria).
 * This interface defines the contract for fetching meals for the next available day.
 */
interface MensaFetcherSpecification {

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
 * @param type The MealType of the meal
 */
data class MensaMeal(
    val name: String, val price: Double, val type: MealType
)

/**
 * Enum representing the type of meal (vegetarian, vegan, or meat).
 */
enum class MealType {
    VEGETARIAN, VEGAN, MEAT;

    companion object {
    }
}

/**
 * Sealed class representing exceptions that can occur during Mensa fetching.
 */
sealed class MensaFetcherException(message: String, cause: Throwable?) : Exception(message, cause) {
    /**
     * Exception for network errors.
     * @param cause The cause of the exception.
     */
    class NetworkException(cause: Throwable?) :
        MensaFetcherException("Network error occurred while fetching Mensa meals!", cause)

    /**
     * Exception for invalid response format.
     * @param expected The expected format description.
     */
    class InvalidResponseFormatException(expected: String) :
        MensaFetcherException("The response format is invalid! Expected: $expected", null)
}
