package se.allco.githubbrowser.common.utils

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

operator fun CompositeDisposable.plusAssign(disposable: Disposable?) {
    add(disposable ?: return)
}

fun <T> Maybe<T>.toSingleOptional(): Single<Optional<T>> =
    map { Optional.of(it) }.defaultIfEmpty(Optional.None)

fun <T : Any> ObservableEmitter<T>.onNextSafely(value: T?) {
    value?.takeIf { !isDisposed }?.also { onNext(it) }
}

fun <T : Any> Observable<T>.delayIfNotNull(
    timeMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io(),
): Observable<T> =
    timeMs.takeIf { it > 0 }?.let { delay(it, unit, scheduler) } ?: this

fun <T : Any> Observable<T>.timeoutFirst(
    timeMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io(),
): Observable<T> = ambWith(Observable.never<T>().timeout(timeMs, unit, scheduler))
