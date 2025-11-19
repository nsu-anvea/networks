package ru.nsu.veretennikov.learning;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

public class E4WeatherByCoordinates {
    public static String getCoordinates(String city) throws InterruptedException {
        sleep(2000);
        return "55.75, 37.61"; // Moscow's coordinates
    }

    public static String getWeatherByCoordinates(String coordinates) throws InterruptedException {
        sleep(3000);
        return "Weather at " + coordinates + ": Sunny, temp = 25";
    }

    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            try {
                String coordinates = getCoordinates("Moscow");
                System.out.println(coordinates);
                return coordinates;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(coordinates -> CompletableFuture.supplyAsync(() -> {
            try {
                return getWeatherByCoordinates(coordinates);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        })).thenAccept(System.out::println).join();
    }
}