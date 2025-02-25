package org.skywalker;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class Chapter6Test {

    private static final Logger log = LoggerFactory.getLogger(Chapter6Test.class);

    @Test
    void testSample() throws InterruptedException {
        Observable<String> names = Observable.just(
                "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan", "Margaret", "Dorothy"
        );

        Observable<Long> delay = Observable
                .just(
                        0.1, 0.6, 0.9, 1.1, 3.3, 3.4, 3.5, 3.6, 4.4, 4.8
                )
                .map(d -> (long) (d * 1000L));

        names
                .zipWith(
                        delay,
                        (n, d) -> Observable.just(n).delay(d, TimeUnit.MILLISECONDS)
                )
                .flatMap(n -> n)
                .sample(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testBuffer() {
        Observable.range(1, 10)
                .buffer(3)
                .subscribe(System.out::println);
    }

    @Test
    void testBufferTime() throws InterruptedException {
        Observable
                .interval(10, TimeUnit.MILLISECONDS)
                .map(tick -> "A" + tick)
                .buffer(20, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);

        TimeUnit.MILLISECONDS.sleep(200);
    }

    @Test
    void testBufferClosing() throws InterruptedException {
        Observable.interval(10, TimeUnit.MILLISECONDS)
                .map(tick -> "A" + tick)
                .buffer(Observable.interval(15, TimeUnit.MILLISECONDS))
                .subscribe(System.out::println);

        TimeUnit.MILLISECONDS.sleep(100);
    }

    @Test
    void testWindow() {
        Observable.range(1, 10)
                .window(3)
                .subscribe(
                        observable -> observable.subscribe(
                                System.out::println,
                                e -> System.out.println("Error!"),
                                () -> System.out.println("Window complete!")
                        ));
    }

    @Test
    void testBackPressure() throws InterruptedException {
        Observable.range(1, 10000)
                .doOnNext(i -> {
                    log.info("Created: {}", i);
                })
                .observeOn(Schedulers.io())
                .subscribe(this::wash);
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    void testBackPressureError() throws InterruptedException {
        Observable.create(
                        emitter -> {
                            for (int i = 0; i < 100000; i++) {
                                emitter.onNext(i);
                            }
                        },
                        Emitter.BackpressureMode.ERROR
                )
                .observeOn(Schedulers.io())
                .subscribe(
                        this::wash,
                        e -> {
                            log.error("Washing error.", e);
                        }
                );
        TimeUnit.SECONDS.sleep(1);
    }

    private void sleepQuietly() {
        try {
            TimeUnit.MILLISECONDS.sleep(40);
        } catch (InterruptedException ignored) {
        }
    }

    private void wash(Object i) {
        log.info("Washing: {}", i);
        sleepQuietly();
    }
}
