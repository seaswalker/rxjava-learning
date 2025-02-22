package org.skywalker;


import rx.Observable;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Observable.from(List.of("A")).doOnNext(System.out::println).subscribe();
    }
}