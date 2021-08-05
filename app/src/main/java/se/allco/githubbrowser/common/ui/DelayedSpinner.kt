package se.allco.githubbrowser.common.ui

import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import se.allco.githubbrowser.common.utils.delayIfNotNull
import se.allco.githubbrowser.common.utils.plusAssign
import se.allco.githubbrowser.common.utils.postValueIfChanged
import se.allco.githubbrowser.common.utils.subscribeSafely
import java.util.concurrent.TimeUnit

const val DELAYED_SPINNER_WARMING_UP_PHASE_MS = 500L
const val DELAYED_SPINNER_LOADING_PHASE_MS = 1000L

// TODO: choose better naming and remove unused functions
/**
 * This extension implements the idea of non-flickering spinners presented at ContentLoadingProgressBar
 * but for RxStreams.
 *
 * Terms:
 *
 * Time
 * 0......................|...................X
 *                        ^ Spinner is shown  ^ Spinner is hidden
 * |<- Warming up phase ->|<- Loading phase ->|<-  release phase
 *
 * From 0 to when the spinner is supposed to be shown — "warming up phase"
 * Right after "warming up phase" starts "loading phase" and lasts until the spinner is supposed to be hidden,
 * And after — "release phase"
 *
 * If emission on the input stream happens during the warming up phase it just lets the emission to go through smoothly.
 * If the emission happens during the loading phase then it will be delayed until the release phase.
 * If emission happens during the release phase then it does nothing.
 * It calls onDelayed(true) when the loading phase is reached.
 * It calls onDelayed(false) when the upstream is completed and the release phase is reached.
 */
fun <T> Observable<T>.addSmartSpinner(
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
    onDelay: (started: Boolean) -> Unit,
): Observable<T> =
    Observable.create<T> { emitter ->
        require(warmingUpPhaseDurationMs >= 0) { "delayedSpinner warmingUpPhaseDurationMs < 0" }
        require(loadingPhaseDurationMs >= 0) { "delayedSpinner loadingPhaseDurationMs < 0" }

        val startTime: Long = scheduler.now(TimeUnit.MILLISECONDS)
        val disposables = CompositeDisposable()
        val timerDisposable = SerialDisposable()
        disposables += timerDisposable
        emitter.setDisposable(disposables)

        timerDisposable.set(
            Observable.timer(warmingUpPhaseDurationMs, TimeUnit.MILLISECONDS, scheduler)
                .firstElement()
                .subscribeSafely { onSuccess = { onDelay(true) } }
        )

        @Suppress("UnstableApiUsage")
        disposables += this
            .materialize()
            .concatWith(Observable.never())
            .concatMap { notification ->
                if (notification.isOnError) throw notification.error!!
                val elapsedTime = scheduler.now(TimeUnit.MILLISECONDS) - startTime
                val delayMs = when {
                    elapsedTime < warmingUpPhaseDurationMs -> 0
                    else -> (loadingPhaseDurationMs - (elapsedTime - warmingUpPhaseDurationMs)).coerceAtLeast(
                        0
                    )
                }

                Observable
                    .just(notification)
                    .delayIfNotNull(delayMs, TimeUnit.MILLISECONDS, scheduler)
                    .doOnNext {
                        timerDisposable.set(Disposable.empty())
                        onDelay(false)
                    }
            }
            .dematerialize { it }
            .subscribeSafely(emitter)
    }

fun <T> Single<T>.addSmartSpinner(
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
    onDelay: (started: Boolean) -> Unit,
): Single<T> =
    toObservable().addSmartSpinner(
        warmingUpPhaseDurationMs = warmingUpPhaseDurationMs,
        loadingPhaseDurationMs = loadingPhaseDurationMs,
        scheduler = scheduler,
        onDelay = onDelay
    ).firstOrError()

fun <T> Maybe<T>.addSmartSpinner(
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
    onDelay: (started: Boolean) -> Unit,
): Maybe<T> =
    toObservable().addSmartSpinner(
        warmingUpPhaseDurationMs = warmingUpPhaseDurationMs,
        loadingPhaseDurationMs = loadingPhaseDurationMs,
        scheduler = scheduler,
        onDelay = onDelay
    ).firstElement()

fun Completable.addSmartSpinner(
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
    onDelay: (started: Boolean) -> Unit,
): Completable =
    toObservable<Any>().addSmartSpinner(
        warmingUpPhaseDurationMs = warmingUpPhaseDurationMs,
        loadingPhaseDurationMs = loadingPhaseDurationMs,
        scheduler = scheduler,
        onDelay = onDelay
    ).ignoreElements()

/**
 * Works like delayedSpinner version with `onDelay` lambda but instead of onDelay, `showSpinner` is used.
 */
fun <T> Observable<T>.addSmartSpinner(
    showSpinner: MutableLiveData<Boolean>,
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
): Observable<T> =
    doOnSubscribe { showSpinner.postValueIfChanged(false) }
        .addSmartSpinner(
            warmingUpPhaseDurationMs = warmingUpPhaseDurationMs,
            loadingPhaseDurationMs = loadingPhaseDurationMs,
            scheduler = scheduler
        ) { delayed -> showSpinner.postValueIfChanged(delayed) }

fun Completable.addSmartSpinner(
    showSpinner: MutableLiveData<Boolean>,
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
): Completable =
    toObservable<Any>()
        .addSmartSpinner(showSpinner, warmingUpPhaseDurationMs, loadingPhaseDurationMs)
        .ignoreElements()

fun <T> Single<T>.addSmartSpinner(
    showSpinner: MutableLiveData<Boolean>,
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
): Single<T> =
    toObservable()
        .addSmartSpinner(showSpinner, warmingUpPhaseDurationMs, loadingPhaseDurationMs)
        .firstOrError()

fun <T> Observable<T>.addSmartLoadingState(
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
    createLoadingState: () -> T,
): Observable<T> {
    val injector = PublishSubject.create<T>()
    return addSmartSpinner(
        warmingUpPhaseDurationMs = warmingUpPhaseDurationMs,
        loadingPhaseDurationMs = loadingPhaseDurationMs,
        scheduler = scheduler,
        onDelay = { showLoading ->
            if (showLoading) {
                injector.onNext(createLoadingState())
            }
        }
    ).mergeWith(injector)
}

fun <T> Single<T>.addSmartLoadingState(
    warmingUpPhaseDurationMs: Long = DELAYED_SPINNER_WARMING_UP_PHASE_MS,
    loadingPhaseDurationMs: Long = DELAYED_SPINNER_LOADING_PHASE_MS,
    scheduler: Scheduler = Schedulers.io(),
    createLoadingState: () -> T,
): Observable<T> {
    return toObservable().addSmartLoadingState(
        warmingUpPhaseDurationMs = warmingUpPhaseDurationMs,
        loadingPhaseDurationMs = loadingPhaseDurationMs,
        scheduler = scheduler,
        createLoadingState = createLoadingState,
    )
}
