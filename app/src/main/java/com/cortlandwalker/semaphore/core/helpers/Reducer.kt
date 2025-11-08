package com.cortlandwalker.semaphore.core.helpers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Core MVI reducer:
 * - Serializes [Action] processing (via a [Mutex])
 * - Owns state mutation hooks provided by [StoreViewModel.bind]
 * - Emits one-off [Effect]s
 * - Provides helpers to collect [Flow]s tied to the **VIEW** lifecycle (see [collectLocalOnce])
 *
 * @param S State type
 * @param A Action type
 * @param E Effect type
 *
 * Implementors typically inject repositories/use-cases and override [process] to handle actions.
 */
abstract class Reducer<S : Any, A : Any, E : Any> {

    // ---------- VM wiring (provided by StoreViewModel.bind) ----------

    /** Read current state. Populated by [bind]. */
    private lateinit var readFn: () -> S

    /** Apply a new state. Populated by [bind]. */
    private lateinit var writeFn: (S) -> Unit

    /** Emit a one-off effect. Populated by [bind]. */
    private lateinit var emitFn: suspend (E) -> Unit

    /** Dispatch an action from within the reducer. Populated by [bind]. */
    private lateinit var postFn: (A) -> Unit

    /** Ensures actions are processed one-at-a-time in order. */
    private val mutex = Mutex()

    /** True once [bind] has been called by the hosting ViewModel. */
    private var isBound = false

    // ---------- Scopes & Lifecycles ----------

    /**
     * Long-lived VM scope (attached by [StoreViewModel]).
     * Use for work that should outlive the Fragment's view (e.g., network, cache sync).
     */
    protected lateinit var scope: CoroutineScope
        private set

    internal fun attachScope(scope: CoroutineScope) {
        this.scope = scope
    }

    /**
     * The current VIEW lifecycle owner. Set by [ReducerFragment] in [ReducerFragment.onViewCreated].
     * May be null before the view is created or after it is destroyed.
     */
    private var viewOwner: LifecycleOwner? = null

    /**
     * Tracks collector keys created for the current view instance to avoid duplicate subscriptions.
     */
    private val perViewCollectors = mutableSetOf<String>()

    /**
     * Queue for [collectLocalOnce] calls invoked before [attachView] is available.
     */
    private data class PendingCollector(
        val key: String,
        val action: (LifecycleOwner) -> Unit
    )

    private val pending = mutableListOf<PendingCollector>()

    // ---------- Binding from StoreViewModel ----------

    /**
     * Bind state/effect hooks and dispatch function provided by the owning [StoreViewModel].
     * This is called once during ViewModel init.
     */
    internal fun bind(
        read: () -> S,
        write: (S) -> Unit,
        emit: suspend (E) -> Unit,
        post: (A) -> Unit
    ) {
        readFn = read
        writeFn = write
        emitFn = emit
        postFn = post
        isBound = true
    }

    // ---------- View lifecycle hand-off ----------

    /**
     * Attach the current **view** lifecycle owner so reducer can create per-view collectors.
     * Called from [ReducerFragment.onViewCreated].
     */
    internal fun attachView(owner: LifecycleOwner) {
        viewOwner = owner
        perViewCollectors.clear()

        // Flush collectors that were queued before the view existed.
        if (pending.isNotEmpty()) {
            val toRun = pending.toList()
            pending.clear()
            toRun.forEach { it.action(owner) }
        }
    }

    // ---------- State & Effects API ----------

    /** Returns the current state (thread-safe via StoreViewModel). */
    protected fun read(): S = readFn()

    /**
     * Atomically transforms current state and writes it back.
     * Prefer using immutable state data classes.
     */
    protected fun state(transform: (S) -> S) = writeFn(transform(read()))

    /** Emits a one-off effect. Collect via StoreViewModel.effects. */
    protected suspend fun emit(effect: E) = emitFn(effect)

    /**
     * Dispatch an action back into this reducer through the ViewModel's queue.
     * Safe to call from any thread. Will serialize through [accept].
     */
    fun postAction(action: A) {
        check(isBound) { "Reducer not bound. Ensure StoreViewModel.bind(...) was called." }
        postFn(action)
    }

    // ---------- Action Processing ----------

    /**
     * Implement action handling here. This is the only place that should mutate state or emit effects.
     * Runs under a [Mutex] to guarantee sequential processing.
     */
    protected abstract suspend fun process(action: A)

    /**
     * Entry point for actions arriving from the ViewModel. Serialized by [mutex].
     */
    internal suspend fun accept(action: A) = mutex.withLock { process(action) }

    // ---------- Optional hooks ----------

    /**
     * Optionally return an initial Action to auto-dispatch when the Fragment's view is created.
     * Useful for kicking off DB/Network loads once per view lifecycle.
     */
    open fun onLoadAction(): A? = null

    /**
     * Override to cancel extra jobs or release resources that are not lifecycle-aware.
     * View/VM scopes are managed externally.
     */
    open fun onCleared() {}

    // ---------- Flow collection helpers ----------

    /**
     * Collect a [flow] tied to the **VIEW** lifecycle, at most once per [key] for the current view instance.
     * - If called before a view exists, the collector is queued and started when the view attaches.
     * - If called repeatedly with the same [key] for the same view, subsequent calls are ignored.
     *
     * @param key Unique key for this collector within the current view instance.
     * @param flow The Flow to collect.
     * @param minActiveState Minimum lifecycle state required to keep collecting (default: STARTED).
     * @param onEach Called for each emission.
     * @param onError Called if the upstream Flow throws, before completion; default no-op.
     */
    protected fun <T> collectLocalOnce(
        key: String,
        flow: Flow<T>,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        onEach: suspend (T) -> Unit,
        onError: suspend (Throwable) -> Unit = {}
    ) {
        val runWithOwner: (LifecycleOwner) -> Unit = { owner ->
            if (perViewCollectors.add(key)) {
                owner.lifecycleScope.launch {
                    owner.lifecycle.repeatOnLifecycle(minActiveState) {
                        flow
                            .catch { onError(it) }
                            .collect { onEach(it) }
                    }
                }
            }
        }

        val owner = viewOwner
        if (owner == null) {
            pending.add(PendingCollector(key, runWithOwner))
        } else {
            runWithOwner(owner)
        }
    }
}
