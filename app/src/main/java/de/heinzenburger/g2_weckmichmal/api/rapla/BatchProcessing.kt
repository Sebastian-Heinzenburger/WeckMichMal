package de.heinzenburger.g2_weckmichmal.api.rapla

/**
 * Represents an element in a batch, paired with a unique ID.
 *
 * @param T The type of the contained value.
 * @property id The unique ID of this element within the batch.
 * @property value The value associated with this ID.
 */
data class BatchTuple<T>(
    val id: Int,
    val value: T
) {
    /**
     * Creates a new [BatchTuple] with the same [id], but a different [value].
     *
     * @param I The type of the new value.
     * @param value The new value to associate with the existing ID.
     * @return A new [BatchTuple] instance with the updated value.
     */
    fun <I> map(value: I): BatchTuple<I> {
        return BatchTuple(id, value)
    }
}

/**
 * A type alias for a collection of [BatchTuple] elements.
 */
typealias Batch<T> = Iterable<BatchTuple<T>>

/**
 * Performs an inner join on two batches based on matching IDs.
 *
 * @param originBatch The original batch containing elements of type [I].
 * @param outputBatch The batch to join with, containing elements of type [O].
 * @return A list of pairs where the IDs matched in both batches, containing values from each batch.
 */
fun <I, O> innerJoinBranches(
    originBatch: Batch<I>,
    outputBatch: Batch<O>
): Iterable<Pair<I, O>> {

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

/**
 * Performs an inner join on two batches, preserving the ID and structure in the result.
 *
 * @param originBatch The original batch containing elements of type [I].
 * @param outputBatch The batch to join with, containing elements of type [O].
 * @return A new batch containing [BatchTuple] elements with paired values from both batches.
 */
fun <I, O> innerJoinBranchesPreserving(
    originBatch: Batch<I>,
    outputBatch: Batch<O>
): Batch<Pair<I, O>> {

    val outputMap = outputBatch.associateBy { it.id }

    return originBatch.mapNotNull { origin ->
        val matchingOutput = outputMap[origin.id]
        if (matchingOutput != null) {
            origin.map(Pair(origin.value, matchingOutput.value))
        } else {
            null
        }
    }
}

/**
 * Creates a [Batch] from a given list by assigning each element a sequential ID.
 *
 * @param list The input list.
 * @return A batch of [BatchTuple] elements with indexed IDs.
 */
fun <T> batchFrom(list: Iterable<T>): Batch<T> {
    return list.mapIndexed { index, value ->
        BatchTuple(id = index, value = value)
    }
}
