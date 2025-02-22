package org.skywalker;

import org.junit.jupiter.api.Test;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.concurrent.TimeUnit;

public class Chapter2Test {

    @Test
    public void testRange() {
        Observable.range(5, 3).subscribe(this::log);
    }

    @Test
    public void testCreate() {
        Observable<Integer> observable = Observable.create(
                emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                },
                Emitter.BackpressureMode.ERROR
        );

        log("Starting...");
        observable.subscribe(this::log);
        log("Exit");
    }

    @Test
    public void testMultiSubscriber() {
        Observable<Integer> observable = simpleObservable();
        log("Starting...");
        observable.subscribe(this::log);
        observable.subscribe(this::log);
        log("Exit");
    }

    @Test
    public void testCache() {
        Observable<Integer> observable = simpleObservable().cache();
        log("Starting...");
        observable.subscribe(this::log);
        observable.subscribe(this::log);
        log("Exit");
    }

    @Test
    public void testUnlimited() throws InterruptedException {
        Observable<Integer> observable = Observable.create(
                emitter -> {
                    Runnable runnable = () -> {
                        int i = 0;
                        while (!Thread.currentThread().isInterrupted()) {
                            emitter.onNext(i);
                            log(i);
                            ++i;
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                log("Interrupted");
                                break;
                            }
                        }
                    };

                    Thread thread = new Thread(runnable);
                    thread.start();

                    emitter.setCancellation(thread::interrupt);

                },
                Emitter.BackpressureMode.ERROR
        );

        Subscription subscription = observable.subscribe(this::log);
        TimeUnit.SECONDS.sleep(5);
        subscription.unsubscribe();
    }

    @Test
    public void testTimer() throws InterruptedException {
        Observable.timer(5, TimeUnit.SECONDS).subscribe(this::log);
        TimeUnit.SECONDS.sleep(6);
    }

    @Test
    public void testInterval() throws InterruptedException {
        Subscription subscription = Observable.interval(1, TimeUnit.SECONDS).subscribe(this::log);
        TimeUnit.SECONDS.sleep(5);
        subscription.unsubscribe();
    }

    @Test
    public void testSubject() throws InterruptedException {
        Subject<Integer, Integer> subject = PublishSubject.create();

        new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
                subject.onNext(i);
            }
        }).start();

        Subscription subscription1 = subject.subscribe(this::log);
        Subscription subscription2 = subject.subscribe(this::log);

        TimeUnit.SECONDS.sleep(6);
        subscription1.unsubscribe();
        subscription2.unsubscribe();
    }

    @Test
    public void testShare() {
        Observable<Integer> observable = simpleObservable().share();
        observable.subscribe(this::log);
        observable.subscribe(this::log);
        observable.subscribe(this::log);
    }

    @Test
    public void testNoSubscriber() {
        simpleObservable().doOnNext(this::log).subscribe();
    }

    @Test
    public void testPublish() {
        ConnectableObservable<Integer> connectableObservable = simpleObservable().publish();
        connectableObservable.subscribe(this::log);
        log("Subscriber 1");
        connectableObservable.subscribe(this::log);
        log("Subscriber 2");
        connectableObservable.connect();
    }

    private Observable<Integer> simpleObservable() {
        return Observable.create(
                emitter -> {
                    log("Create");
                    emitter.onNext(1);
                },
                Emitter.BackpressureMode.ERROR
        );
    }

    private void log(Object msg) {
        System.out.printf("%s: %s%n", Thread.currentThread().getName(), msg);
    }

}
