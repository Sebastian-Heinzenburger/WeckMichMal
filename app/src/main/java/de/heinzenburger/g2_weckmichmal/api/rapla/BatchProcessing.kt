package de.heinzenburger.g2_weckmichmal.api.rapla

data class BatchTuple<T>(
    val id: Int,
    val value: T
) {
    fun <I>map(value: I): BatchTuple<I>{
        return BatchTuple(id, value)
    }
}

typealias Batch<T> = Iterable<BatchTuple<T>>

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

fun <T>batchFrom(list: Iterable<T>) : Batch<T> {
    return list.mapIndexed { index, value ->
        BatchTuple(id = index, value = value)
    }
}
