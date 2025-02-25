package org.skywalker;

import org.junit.jupiter.api.Test;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Chapter7Test {

    @Test
    void testTimeout() throws InterruptedException {
        Observable
                .just("E")
                .delay(50, TimeUnit.MILLISECONDS)
                .concatWith(Observable.<String>empty().delay(200, TimeUnit.MILLISECONDS))
                .timeout(300, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println, System.out::println);

        TimeUnit.MILLISECONDS.sleep(500);
    }

}
