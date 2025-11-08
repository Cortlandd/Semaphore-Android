package com.cortlandwalker.semaphore.core.helpers

import retrofit2.HttpException
import java.io.IOException
import kotlinx.serialization.SerializationException

/**
 * # Outcome & outcome{}
 *
 * Lightweight result type for **success or failure** without throwing. Great for
 * repository ↔ ViewModel ↔ UI pipelines and for mapping exceptions to UI‑friendly states.
 *
 * ## Use cases
 * - Wrap Retrofit calls, Room operations, or any suspend work
 * - Emit `Outcome.Ok(value)` on success, `Outcome.Err(error)` on failure
 *
 * ## Example
 * ```kotlin
 * val res: Outcome<Vehicle> = outcome { api.decodeVin(vin) }
 * when (res) {
 *   is Outcome.Ok -> show(res.value)
 *   is Outcome.Err -> showError(res.error.message)
 * }
 *
 * // In a Flow:
 * flow {
 *   emit(Outcome.Ok(local.first()))
 *   emit(outcome { api.fetch() })
 * }
 * ```
 */
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val error: Throwable) : Outcome<Nothing>
}

suspend inline fun <T> outcome(
    crossinline block: suspend () -> T
): Outcome<T> = try {
    Outcome.Ok(block())
} catch (e: HttpException) {
    Outcome.Err(e)   // use e.code(), e.response(), etc. in UI layer
} catch (e: IOException) {
    Outcome.Err(e)   // network error
} catch (e: SerializationException) {
    Outcome.Err(e)   // JSON parsing error
} catch (t: Throwable) {
    Outcome.Err(t)   // fallback
}
