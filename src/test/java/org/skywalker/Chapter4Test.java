package org.skywalker;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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

}
