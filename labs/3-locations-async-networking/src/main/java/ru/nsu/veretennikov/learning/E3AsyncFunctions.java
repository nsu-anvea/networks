package ru.nsu.veretennikov.learning;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

public class E3AsyncFunctions {
    public static String getWeather() throws InterruptedException {
        sleep(2000);
        return "Sunny, temp = 25";
    }
    public static String getPlaces() throws InterruptedException {
        sleep(3000);
        return "Museum, Park";
    }
    public static void main(String[] args) throws InterruptedException {
        version1();
        sleep(1000);
        version2();
    }

    public static void version1() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return getWeather();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return getPlaces();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        future1.thenAccept(weather -> System.out.println("Weather: " + weather));
        future2.thenAccept(places -> System.out.println("Places: " + places));

        /*
         * This is better than
         * future1.join();
         * future2.join();
         * because allOf wait for everyone at once
         */
        CompletableFuture.allOf(future1, future2).join();
    }

    public static void version2() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return getWeather();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return getPlaces();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        /*
         * This is better than
         * future1.join();
         * future2.join();
         * because allOf wait for everyone at once
         */
        CompletableFuture.allOf(future1, future2).join();

        System.out.println("Weather: " + future1.join());
        System.out.println("Places: " + future2.join());
    }
}