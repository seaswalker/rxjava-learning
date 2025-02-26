package org.skywalker;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Chapter7Test {

    private static final Logger log = LoggerFactory.getLogger(Chapter7Test.class);

    @Test
    void testTimeout() throws InterruptedException {
        Observable
                .just("E")
                .delay(50, TimeUnit.MILLISECONDS)
                .concatWith(Observable.<String>empty().delay(200, TimeUnit.MILLISECONDS))
                .timeout(100, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println, System.out::println);

        TimeUnit.MILLISECONDS.sleep(500);
    }

    @Test
    void testRetry() {
        Observable.create(
                        emitter -> {
                            log.info("Creating Called");
                            throw new IllegalStateException();
                        },
                        Emitter.BackpressureMode.ERROR
                )
                .retry(1)
                .subscribe(System.out::println, System.out::println);
    }

}
