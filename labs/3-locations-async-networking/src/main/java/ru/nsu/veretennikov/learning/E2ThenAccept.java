package ru.nsu.veretennikov.learning;

import java.util.concurrent.CompletableFuture;

public class E2ThenAccept {
    public static void main(String[] args) {
        example1();
        example2();
    }

    public static void example1() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {}
            return "Result";
        });
        future.thenAccept(result -> System.out.println("Got: " + result));
        future.join();
    }

    public static void example2() {
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {}
            return 10;
        }).thenApply(number -> {
            System.out.println("thenApply thread: " + Thread.currentThread().getName());
            return number + 5;
        }).thenApply(number -> {
            System.out.println("thenApply thread: " + Thread.currentThread().getName());
            return number * 3;
        }).thenAccept(result -> {
            System.out.println("thenAccept thread: " + Thread.currentThread().getName());
        }).join();
    }
}
