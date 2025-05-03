package de.heinzenburger.g2_weckmichmal.api.rapla

data class BatchTuple<T>(
    val id: Long,
    val value: T
) {
    fun isSameBatch(otherTuple: BatchTuple<*>): Boolean {
        return id == otherTuple.id
    }
}

fun <I, O> mergeBatches(
    originBatch: List<BatchTuple<I>>,
    outputBatch: List<BatchTuple<O>>
): List<Pair<I, O>> {

    val outputMap = outputBatch.associateBy { it.id }

    return originBatch.mapNotNull { origin ->
        val matchingOutput = outputMap[origin.id]
        if (matchingOutput != null) {
            Pair(origin.value, matchingOutput.value)
        } else {
            null
        }
    }
}
