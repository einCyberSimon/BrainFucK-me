package einsimon.de.other

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class Result<out V, out E>

class Success<out V>(val value: V) : Result<V, Nothing>()

class Error<out E>(val error: E) : Result<Nothing, E>()

@OptIn(ExperimentalContracts::class)
inline infix fun <V, E, T> Result<V, E>.flatMap(transform: (V) -> Result<T, E>): Result<T, E> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return when (this) {
        is Success -> transform(value)
        is Error -> this
    }
}

@OptIn(ExperimentalContracts::class)
inline infix fun <V, E, T> Result<V, E>.map(transform: (V) -> T): Result<T, E> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return when (this) {
        is Success -> Success(transform(value))
        is Error -> this
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <V> withCatching(block: () -> V): Result<V, Throwable> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return try {
        Success(block())
    } catch (error: Throwable) {
        Error(error)
    }
}

@OptIn(ExperimentalContracts::class)
inline infix fun <V, E> Result<V, E>.getOrElse(transform: (E) -> V): V {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return when (this) {
        is Success -> value
        is Error -> transform(error)
    }
}

@OptIn(ExperimentalContracts::class)
inline infix fun <V, E> Result<V, E>.onSuccessAlso(block: (V) -> Unit): Result<V, E> {
    when (this) {
        is Success -> block(value)
        is Error -> Unit
    }
    return this
}
