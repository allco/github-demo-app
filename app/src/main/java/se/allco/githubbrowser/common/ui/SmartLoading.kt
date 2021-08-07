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
import java.util.concurrent.TimeUnit

const val SMART_SPINNER_PHASE_WARMING_UP_MS = 500L
const val SMART_SPINNER_PHASE_LOADING_MS = 1000L

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
 * If the emission happens during the loading phase then the emission will be delayed until the release phase.
 * If emission happens during the release phase then the emission goes through smoothly.
 * It calls onShowLoading() when the loading phase is reached and the spinner is supposed to be shown.
 * It calls onHideLoading() when the upstream is completed and the release phase is reached.
 */

class SmartLoadingConfiguration<T> {
    var warmingUpPhaseDurationMs: Long = SMART_SPINNER_PHASE_WARMING_UP_MS
    var loadingPhaseDurationMs: Long = SMART_SPINNER_PHASE_LOADING_MS
    var scheduler: Scheduler = Schedulers.io()
    var onShowLoading: (() -> Unit)? = null
    var onHideLoading: (() -> Unit)? = null
    var onSwitchLoading: ((show: Boolean) -> Unit)? = null
    var showLoadingLiveData: MutableLiveData<Boolean>? = null
    var onShowLoadingEmitter: (() -> T)? = null
}

fun <T> Observable<T>.attachSmartLoading(configBlock: SmartLoadingConfiguration<T>.() -> Unit): Observable<T> {
    val config = SmartLoadingConfiguration<T>().apply(configBlock)
    @Suppress("DEPRECATION")
    return attachSmartLoading(
        config.warmingUpPhaseDurationMs,
        config.loadingPhaseDurationMs,
        config.scheduler,
        onShowLoading = {
            config.onShowLoading?.invoke()
            config.onSwitchLoading?.invoke(true)
            config.showLoadingLiveData?.postValueIfChanged(true)
        },
        onHideLoading = {
            config.onHideLoading?.invoke()
            config.onSwitchLoading?.invoke(false)
            config.showLoadingLiveData?.postValueIfChanged(false)
        }
    ).run {
        val emitter = config.onShowLoadingEmitter ?: return@run this
        val injector = PublishSubject.create<T>()
        val onShowLoadingOrig = config.onShowLoading
        config.onShowLoading = {
            injector.onNext(emitter())
            onShowLoadingOrig?.invoke()
        }
        this.mergeWith(injector)
    }
}

fun <T> Single<T>.attachSmartLoading(configBlock: SmartLoadingConfiguration<T>.() -> Unit): Single<T> =
    toObservable().attachSmartLoading(configBlock).firstOrError()

fun <T> Maybe<T>.attachSmartLoading(configBlock: SmartLoadingConfiguration<T>.() -> Unit): Maybe<T> =
    toObservable().attachSmartLoading(configBlock).firstElement()

fun Completable.attachSmartLoading(configBlock: SmartLoadingConfiguration<Any>.() -> Unit): Completable =
    toObservable<Any>().attachSmartLoading(configBlock).ignoreElements()

private fun <T> Observable<T>.attachSmartLoading(
    warmingUpPhaseDurationMs: Long = SMART_SPINNER_PHASE_WARMING_UP_MS,
    loadingPhaseDurationMs: Long = SMART_SPINNER_PHASE_LOADING_MS,
    scheduler: Scheduler = Schedulers.io(),
    onShowLoading: () -> Unit,
    onHideLoading: () -> Unit = {},
): Observable<T> =
    Observable.create { emitter ->
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
                .subscribe { onShowLoading() }
        )

        @Suppress("UnstableApiUsage", "NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER")
        disposables += this
            .materialize()
            .concatWith(Observable.never())
            .concatMap { notification ->
                if (notification.isOnError) throw requireNotNull(notification.error)
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
                        onHideLoading()
                    }
            }
            .dematerialize { it }
            .subscribe(emitter::onNext, emitter::onError, emitter::onComplete)
    }
