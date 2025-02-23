package org.skywalker;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Chapter4Test {

    private static final Logger logger = LoggerFactory.getLogger(Chapter4Test.class);

    @Test
    void testBlocking() {
        List<Integer> list = Observable.just(1, 2).toList().toBlocking().single();
        logger.info("List: {}.", list);
    }

    @Test
    void testDefer() {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        Observable<Integer> deferred = Observable.defer(() -> Observable.range(atomicInteger.get(), 3));
        atomicInteger.set(3);
        deferred.subscribe(i -> logger.info("I: {}.", i));
    }

    @Test
    void testSubscribeOn() {
        Observable<String> firstName = Observable
                .<String>create(
                        emitter -> {
                            logger.info("Making first name...");
                            emitter.onNext("John");
                        },
                        Emitter.BackpressureMode.ERROR
                )
                .subscribeOn(Schedulers.io());
        Observable<String> secondName = Observable
                .<String>create(
                        emitter -> {
                            logger.info("Making second name...");
                            emitter.onNext("Stones");
                        },
                        Emitter.BackpressureMode.ERROR
                )
                .subscribeOn(Schedulers.io());

        Observable
                .zip(firstName, secondName, (first, second) -> first + " " + second)
                .subscribe(s -> logger.info("Name: {}.", s));
    }

    @Test
    void testParallel() {
        Observable
                .just("Spring", "Vert.x", "Jersey", "IO")
                .flatMap(
                        appName -> Observable
                                .fromCallable(() -> queryAppOwner(appName))
                                 .ignoreElements()
                                .doOnCompleted(() -> logger.info("Completed: `{}`.", appName))
                                .doOnError(e -> logger.error("Failed to query app owner for `{}`.", appName))
                                .onErrorReturn(throwable -> appName)
                                .subscribeOn(Schedulers.io())
                )
                .subscribe(failed -> logger.info("Failed: {}.", failed));
    }

    private String queryAppOwner(String appName) {
        if (appName.length() < 3) {
            throw new IllegalArgumentException("App name length must be at least 3");
        }
        return "skywalker";
    }

}
