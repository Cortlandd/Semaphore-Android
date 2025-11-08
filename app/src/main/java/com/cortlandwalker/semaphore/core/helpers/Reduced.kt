package com.cortlandwalker.semaphore.core.helpers
/**
* # Reduced
*
* Small container returned by reducers: the **new `state`** plus a list of **effect tokens**
* to be executed outside the pure reducer.
*
* ## Example
* ```kotlin
* fun reducer(state: S, action: A): Reduced<S, A> =
*   when (action) {
*     is A.Load -> state.copy(loading = true) reducedWith listOf(A.DoFetch)
*   }
* ```
*/

data class Reduced<S, E>(
    val state: S,  // UI state
    val effects: List<E> = emptyList()  // One-off side-effect channel
) {
    companion object {
        fun <S, E> just(state: S) = Reduced<S, E>(state, emptyList())
    }
}