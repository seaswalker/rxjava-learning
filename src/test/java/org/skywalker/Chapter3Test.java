package org.skywalker;

import io.vavr.Tuple2;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;

public class Chapter3Test {

    private static final Logger logger = LoggerFactory.getLogger(Chapter3Test.class);

    @Test
    public void testMap() {
        Observable<Integer> original = Observable.range(1, 5);

        Observable<Integer> modified = Observable.range(1, 5).map(i -> i + 3);
        System.out.println("Modified: ");
        modified.subscribe(System.out::println);

        System.out.println("Original: ");
        original.subscribe(System.out::println);
    }

    @Test
    public void testFlatMap() {
        Observable.range(1, 1).flatMap(i -> Observable.just(i - 1, i + 1)).subscribe(System.out::println);
    }

    @Test
    public void testDelay() throws InterruptedException {
        Observable
                .range(1, 3)
                .delay(1, TimeUnit.SECONDS)
                .subscribe(i -> log("subscriber", i));

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testDelay2() throws InterruptedException {
        Observable.just("word", "up", "holiday")
                .delay(s -> Observable.timer(s.length(), TimeUnit.SECONDS))
                .subscribe(s -> log("subscriber", s));

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void testDelay3() throws InterruptedException {
        Observable.just("word", "up", "holiday")
                .flatMap(s -> Observable.timer(s.length(), TimeUnit.SECONDS).map(delay -> s))
                .subscribe(s -> log("subscribe", s));

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void testFlatOrder() throws InterruptedException {
        Observable.just(10, 1)
                .flatMap(integer -> Observable
                        .just(integer)
                        .delay(integer, TimeUnit.SECONDS))
                .subscribe(i -> log("subscriber", i));

        TimeUnit.SECONDS.sleep(12);
    }

    @Test
    void testConcatMap() throws InterruptedException {
        Observable.just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .concatMap(this::loadRecordsFor)
                .subscribe(s -> log("subscribe", s));

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testZip() {
        Observable<String> firstName = Observable.just("John", "Smith", "Jane");
        Observable<String> lastName = Observable.just("Doe", "Walker", "Tom");
        firstName
                .zipWith(lastName, (f, s) -> f + " " + s)
                .subscribe(s -> log("subscriber", s));
    }

    @Test
    void testCombineLatest() throws InterruptedException {
        Observable<Long> fast = Observable.interval(10, TimeUnit.MILLISECONDS);
        Observable<Long> slow = Observable.interval(17, TimeUnit.MILLISECONDS);
        Observable
                .combineLatest(fast, slow, (f, s) -> String.format("F%d:S%s", f, s))
                .subscribe(s -> log("subscriber", s));

        TimeUnit.MILLISECONDS.sleep(50);
    }

    @Test
    void testScan() {
        Observable.range(1, 10).scan(Integer::sum).takeLast(1).subscribe(i -> System.out.println("Total: " + i));
    }

    @Test
    void testReduce() {
        Observable.range(1, 10).reduce(Integer::sum).subscribe(i -> System.out.println("Total: " + i));
    }

    @Test
    void testCollect() {
        Observable.just("Hello", "Ming")
                .collect(
                        StringBuilder::new,
                        (sb, s) -> sb.append(s).append(" ")
                )
                .subscribe(s -> log("Result", s));
    }

    @Test
    void testToList() {
        Observable.just("A", "B", "C").toList().subscribe(c -> log("List", c));
    }

    @Test
    void testDistinct() {
        Observable.just(1, 3, 5, 3, 1).distinct().subscribe(i -> log("Distinct", i));
    }

    @Test
    void testDistinctUntilChanged() {
        Observable.just(1, 3, 3, 5, 1).distinctUntilChanged().subscribe(i -> log("DistinctUntilChanged", i));
    }

    @Test
    void testTake() {
        Observable.just(1, 3, 5, 6, 7, 8).takeFirst(integer -> integer > 7).subscribe(i -> log("takeFirst", i));
    }

    @Test
    void testGroupBy() {
        Observable.just(1, 2, 3, 4)
                .groupBy(i -> (i % 2 == 0) ? "even" : "odd")
                .subscribe(
                        groupedObservable -> groupedObservable
                                .doOnSubscribe(() -> logger.info("Key: {} subscribed.", groupedObservable.getKey()))
                                .subscribe(i -> logger.info("KV: [{}:{}].", groupedObservable.getKey(), i))
                );
    }

    @Test
    void testCompose() {
        Observable
                .just(1, 2, 3, 4)
                .compose(
                        observable -> observable
                                .zipWith(
                                        Observable.just(true, false).repeat(),
                                        Tuple2::new
                                )
                                .filter(Tuple2::_2)
                                .map(Tuple2::_1))
                .subscribe(i -> logger.info("I: {}.", i));
    }

    /**
     * Customize operator.
     */
    @Test
    void testLift() {
        Observable.Operator<String, Integer> toStringOperator = subscriber -> new Subscriber<>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(Integer i) {
                subscriber.onNext("S" + i);
            }
        };
        Observable.just(1, 2, 3, 4).lift(toStringOperator).subscribe(s -> logger.info("Lift: {}.", s));
    }

    private Observable<String> loadRecordsFor(DayOfWeek dow) {
        if (DayOfWeek.SUNDAY == dow) {
            return Observable.interval(90, TimeUnit.MILLISECONDS)
                    .take(5)
                    .map(i -> "Sun" + i);
        }

        return Observable.interval(65, TimeUnit.MILLISECONDS)
                .take(5)
                .map(i -> "Mon" + i);
    }

    private void log(String key, Object message) {
        System.out.printf("[%s]-> %s: %s%n", Thread.currentThread().getName(), key, message);
    }

}
